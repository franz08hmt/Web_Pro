/* Run against the existing static server. Requires Playwright in NODE_PATH. */
const assert = require("node:assert/strict");
const path = require("node:path");
const fs = require("node:fs");
const { chromium } = require("playwright");
const base = process.env.BASE_URL || "http://127.0.0.1:4173";
const pages = ["index.html", "pages/mau-robot.html", "pages/linh-kien.html", "pages/lap-rap.html", "pages/thu-vien.html", "pages/thanh-vien.html"];

(async () => {
  const browser = await chromium.launch({ channel: "chrome", headless: true });
  try {
    const page = await browser.newPage();
    const errors = [];
    page.on("pageerror", error => errors.push(error.message));
    page.on("console", message => { if (["error", "warning"].includes(message.type())) errors.push(message.text()); });
    page.on("response", response => { if (response.status() >= 400) errors.push(response.status() + " " + response.url()); });
    for (const width of [320, 768, 1024, 1440]) {
      await page.setViewportSize({ width, height: 1000 });
      for (const file of pages) {
        await page.goto(base + "/" + file);
        await page.evaluate(() => document.fonts.ready);
        for (const img of await page.locator("img").all()) {
          await img.scrollIntoViewIfNeeded();
          await img.evaluate(el => el.decode().catch(() => {}));
        }
        await page.evaluate(() => scrollTo(0, 0));
        console.log(width + "px " + file);
        const problems = await page.evaluate(() => {
          const problems = [];
          if (document.documentElement.scrollWidth > innerWidth + 1) problems.push("horizontal overflow");
          if (document.querySelectorAll("h1").length !== 1) problems.push("h1 count");
          if (!document.querySelector("main[id]")) problems.push("main landmark");
          const ids = [...document.querySelectorAll("[id]")].map(el => el.id);
          if (new Set(ids).size !== ids.length) problems.push("duplicate IDs");
          let previous = 0;
          for (const h of document.querySelectorAll("main h1, main h2, main h3, main h4")) {
            const level = Number(h.tagName[1]);
            if (level > previous + 1) problems.push("heading skip: " + h.textContent);
            previous = level;
          }
          for (const img of document.images) {
            if (!img.hasAttribute("alt") || !img.naturalWidth) problems.push("image: " + img.src);
          }
          if (document.querySelector("figure.card-image h2, figure.card-image h3")) problems.push("unclosed image figure");
          for (const svg of document.querySelectorAll(".nav-list svg")) {
            if (svg.getBoundingClientRect().width > 30) problems.push("unbounded icon");
          }
          if (!document.fonts.check('400 14px Poppins')) problems.push("Poppins font not loaded");
          return problems;
        });
        assert.deepEqual(problems, [], width + "px " + file);
        if (width === 1440) {
          const urls = await page.locator("a[href]").evaluateAll(links => [...new Set(links.map(a => a.href))]);
          for (const url of urls.filter(url => url.startsWith(base + "/"))) {
            assert.equal((await page.request.get(url)).ok(), true, "Internal link: " + url);
          }
        }
        if (process.env.AXE_PATH && [320, 1440].includes(width)) {
          await page.addScriptTag({ path: process.env.AXE_PATH });
          const violations = await page.evaluate(async () => (await axe.run({ runOnly: { type: "tag", values: ["wcag2a", "wcag2aa", "wcag21aa"] } })).violations.map(v => ({ id: v.id, nodes: v.nodes.map(n => n.target) })));
          assert.deepEqual(violations, [], "Accessibility: " + file);
        }
        if (process.env.ARTIFACT_DIR && [320, 1440].includes(width)) {
          fs.mkdirSync(process.env.ARTIFACT_DIR, { recursive: true });
          await page.screenshot({ path: path.join(process.env.ARTIFACT_DIR, path.basename(file, ".html") + "-" + width + ".png"), fullPage: true });
        }
      }
    }
    await page.goto(base + "/pages/linh-kien.html");
    for (const [filter, count] of [["sensor", 2], ["controller", 2], ["motion", 3], ["utility", 2], ["all", 9]]) {
      await page.locator('[data-filter="' + filter + '"]').click();
      assert.equal(await page.locator("#component-catalog article:visible").count(), count, filter);
      assert.equal(await page.locator('[data-filter="' + filter + '"]').getAttribute("aria-pressed"), "true");
    }
    await page.goto(base + "/pages/mau-robot.html");
    await page.locator("#robot-search").fill("khong-co-mau-nay");
    assert.equal(await page.locator("#robot-catalog article").count(), 0);
    assert.equal(await page.locator("#robot-empty").isVisible(), true);
    await page.locator("#robot-search").fill("tránh vật cản");
    assert.equal(await page.locator("#robot-catalog article").count(), 1);
    await page.locator("#robot-catalog a").click();
    assert.equal(await page.locator("#model-select").inputValue(), "obstacle-avoider");
    const models = await page.locator("#model-select option").evaluateAll(items => items.map(item => item.value));
    for (const model of models) {
      await page.locator("#model-select").selectOption(model);
      assert.equal(await page.locator("#parts-progress").getAttribute("value"), "0");
      assert.equal(await page.locator("#model-preview-image").count(), 1, "Model preview is available");
      assert.equal(await page.locator("#model-preview-image").evaluate(img => img.complete && img.naturalWidth > 0), true);
      const boxes = page.locator("#parts-checklist input");
      for (let i = 0; i < await boxes.count(); i++) await boxes.nth(i).check();
      assert.equal(await page.locator("#parts-progress").getAttribute("value"), "100");
      assert.equal(await page.locator("#assembly-steps").evaluate(el => el.classList.contains("is-ready")), true);
      await boxes.first().uncheck();
      assert.equal(await page.locator("#assembly-steps").evaluate(el => el.classList.contains("is-ready")), false);
    }
    await page.goto(base + "/index.html");
    await page.keyboard.press("Tab");
    assert.equal(await page.locator(".skip-link").evaluate(el => el === document.activeElement), true);
    await page.keyboard.press("Enter");
    assert.equal(await page.locator("main").evaluate(el => el === document.activeElement), true);
    assert.deepEqual(errors, [], "Browser errors");
    console.log("PASS: 24 responsive page checks; images, headings, local fonts, filters, search, 3 assembly models, progress and keyboard.");
  } finally { await browser.close(); }
})().catch(error => { console.error(error); process.exitCode = 1; });
