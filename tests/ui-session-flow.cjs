"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const http = require("node:http");
const path = require("node:path");
const vm = require("node:vm");
const { chromium } = require("playwright-core");
const { createApp } = require("../server/app");
const { loadConfig } = require("../server/config/env");

const projectRoot = path.resolve(__dirname, "..");
const context = { window: {} };
vm.runInNewContext(fs.readFileSync(path.join(projectRoot, "assets/js/data.js"), "utf8"), context);
const model = context.window.ROBOT_MODELS.find((item) => item.id === "line-follower");
const steps = model.steps.map((instruction, index) => ({
  id: `line-follower-step-${index + 1}`, robotId: model.id,
  title: `Bước ${index + 1}`, instruction, stepOrder: index + 1
}));
const now = "2026-09-19T00:00:00.000Z";
const sessions = {
  "42": {
    id: "42", userId: "7", robotId: model.id, status: "IN_PROGRESS", progressPercent: 100,
    components: model.parts.map((part) => ({ componentId: part.id, isPrepared: true })),
    steps: [], assembledPartIds: ["arduino-uno"], createdAt: now, updatedAt: now
  },
  "43": {
    id: "43", userId: "7", robotId: model.id, status: "PREPARING",
    progressPercent: Math.floor(100 / model.parts.length),
    components: [{ componentId: model.parts[0].id, isPrepared: true }],
    steps: [], assembledPartIds: [], createdAt: now, updatedAt: now
  }
};
for (let id = 44; id <= 47; id += 1) {
  sessions[String(id)] = { ...structuredClone(sessions["43"]), id: String(id) };
}
let unexpectedCreates = 0;

function list(data) {
  return { data, meta: { page: 1, pageSize: 100, total: data.length } };
}

async function respond(route) {
  const request = route.request();
  const url = new URL(request.url());
  const pathName = url.pathname.slice(4);
  let payload;
  let status = 200;
  if (pathName === "/auth/me") {
    payload = { data: { user: { id: "7", fullName: "Người thử nghiệm", email: "test@example.invalid", role: "USER" } }, csrfToken: "test-csrf" };
  } else if (pathName === "/robots") {
    payload = list([{ ...model, image: model.image.replace(/^\.\./, "") }]);
  } else if (pathName === "/components") {
    payload = list(model.parts.map((part) => ({ id: part.id, name: part.name })));
  } else if (pathName === `/robots/${model.id}/components`) {
    payload = { data: model.parts.map((part) => ({ componentId: part.id, quantity: part.quantity })) };
  } else if (pathName === `/robots/${model.id}/steps`) {
    payload = list(steps);
  } else if (pathName === "/assembly-sessions" && request.method() === "GET") {
    const page = Number(url.searchParams.get("page") || 1);
    const pageSize = Number(url.searchParams.get("pageSize") || 20);
    const records = Object.values(sessions).map(({ components, steps: savedSteps, assembledPartIds, ...summary }) => ({
      ...summary, completedStepCount: savedSteps.filter((step) => step.status === "COMPLETED").length,
      totalStepCount: steps.length
    }));
    payload = { data: records.slice((page - 1) * pageSize, page * pageSize), meta: { page, pageSize, total: records.length } };
  } else if (pathName === "/assembly-sessions" && request.method() === "POST") {
    unexpectedCreates += 1;
    status = 500;
    payload = { error: { code: "UNEXPECTED_CREATE", message: "A saved session must be opened by ID." } };
  } else if (/^\/assembly-sessions\/\d+$/.test(pathName)) {
    const id = pathName.split("/")[2];
    if (sessions[id]) payload = { data: sessions[id] };
    else {
      status = 404;
      payload = { error: { code: "NOT_FOUND", message: "Không tìm thấy phiên lắp ráp." } };
    }
  } else if (/^\/assembly-sessions\/\d+\/visual-parts\/[a-z0-9-]+$/.test(pathName)) {
    const [, , id, , componentId] = pathName.split("/");
    const ids = new Set(sessions[id].assembledPartIds);
    if (request.postDataJSON().isAssembled) ids.add(componentId);
    else ids.delete(componentId);
    sessions[id].assembledPartIds = [...ids];
    payload = { data: sessions[id] };
  } else {
    status = 404;
    payload = { error: { code: "NOT_FOUND", message: pathName } };
  }
  await route.fulfill({ status, contentType: "application/json", body: JSON.stringify(payload) });
}

(async () => {
  const app = createApp({ config: loadConfig({ NODE_ENV: "test" }), database: null, logger: { info() {}, error() {} } });
  const server = http.createServer(app);
  await new Promise((resolve) => server.listen(0, "127.0.0.1", resolve));
  const base = `http://127.0.0.1:${server.address().port}`;
  const browser = await chromium.launch({ channel: "chrome", headless: true });
  try {
    const first = await browser.newContext();
    const second = await browser.newContext();
    await first.route("**/api/**", respond);
    await second.route("**/api/**", respond);
    const page = await first.newPage();
    const browserErrors = [];
    page.on("pageerror", (error) => browserErrors.push(error.message));
    await page.goto(`${base}/pages/tai-khoan.html`);
    await page.locator("#account-history li").first().waitFor();
    assert.equal(await page.locator("#account-history li").count(), 5);
    await page.locator("#account-history-more").click();
    await page.locator("#account-history li").nth(5).waitFor();
    assert.equal(await page.locator("#account-history li").count(), 6);
    const accountImage = page.locator("#account-history li img").first();
    await accountImage.scrollIntoViewIfNeeded();
    await accountImage.evaluate((image) => image.decode());
    assert.equal(await accountImage.evaluate((image) => image.naturalWidth > 0), true);
    if (process.env.ARTIFACT_DIR) {
      fs.mkdirSync(process.env.ARTIFACT_DIR, { recursive: true });
      await page.screenshot({ path: path.join(process.env.ARTIFACT_DIR, "phase1-account.png"), fullPage: true });
    }
    assert.match(await page.locator("#account-history li").first().innerText(), /Chuẩn bị linh kiện: 100%/);
    assert.match(await page.locator("#account-history li").first().innerText(), new RegExp(`Bước lắp ráp: 0/${steps.length}`));
    await page.setViewportSize({ width: 390, height: 800 });
    assert.equal(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth + 1), true);
    await page.setViewportSize({ width: 1280, height: 900 });
    await page.locator('#account-history a[href*="session=42"]').click();
    await page.locator('#assembly-3d-parts input[data-part-id="arduino-uno"]:checked').waitFor();
    await page.locator('#assembly-3d-parts input[data-part-id="dc-motor"]').check();
    await page.locator("#assembly-3d-sync-status").getByText("Đã lưu mô hình 3D vào tài khoản.").waitFor();
    assert.ok(sessions["42"].assembledPartIds.includes("dc-motor"));

    const otherPage = await second.newPage();
    otherPage.on("pageerror", (error) => browserErrors.push(error.message));
    await otherPage.goto(`${base}/pages/lap-rap-3d.html?model=line-follower&session=42`);
    await otherPage.locator('#assembly-3d-parts input[data-part-id="dc-motor"]:checked').waitFor();
    assert.equal(await otherPage.locator("#assembly-3d-parts input:checked").count(), 2);

    await otherPage.goto(`${base}/pages/tai-khoan.html`);
    await otherPage.locator('#account-history a[href*="session=43"]').click();
    await otherPage.locator("#parts-checklist input").first().waitFor();
    assert.equal(await otherPage.locator("#parts-checklist input").first().isChecked(), true);
    assert.equal(await otherPage.locator("#parts-checklist input").nth(1).isChecked(), false);
    assert.match(otherPage.url(), /session=43/);
    if (process.env.ARTIFACT_DIR) {
      fs.mkdirSync(process.env.ARTIFACT_DIR, { recursive: true });
      await page.screenshot({ path: path.join(process.env.ARTIFACT_DIR, "phase1-3d.png"), fullPage: true });
      await otherPage.screenshot({ path: path.join(process.env.ARTIFACT_DIR, "phase1-resume.png"), fullPage: true });
    }
    await otherPage.goto(`${base}/pages/lap-rap-3d.html?model=line-follower&session=999`);
    await otherPage.locator("#assembly-3d-sync-status").getByText(/Không thể mở phiên đã chọn/).waitFor();
    assert.equal(await otherPage.locator('#assembly-3d-parts input[data-part-id="arduino-uno"]').isDisabled(), true);
    assert.equal(unexpectedCreates, 0, "Opening an existing session must never create another one");
    assert.deepEqual(browserErrors, []);

    console.log("PASS: account history, exact session resume, and cross-browser 3D visual restoration.");
  } finally {
    await browser.close();
    await new Promise((resolve, reject) => server.close((error) => error ? reject(error) : resolve()));
  }
})().catch((error) => { console.error(error); process.exitCode = 1; });
