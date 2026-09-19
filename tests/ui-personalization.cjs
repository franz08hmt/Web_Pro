"use strict";

const assert = require("node:assert/strict");
const fs = require("node:fs");
const http = require("node:http");
const path = require("node:path");
const { chromium } = require("playwright-core");
const { createApp } = require("../server/app");
const { loadConfig } = require("../server/config/env");

const session = {
  id: "42", robotId: "line-follower", status: "IN_PROGRESS",
  progressPercent: 75, completedStepCount: 2, totalStepCount: 5,
  updatedAt: "2026-09-19T08:00:00.000Z"
};

async function mockSessionApi(context, { user, items = [], listStatus = 200 }) {
  await context.route("**/api/auth/me", (route) => route.fulfill({
    status: user ? 200 : 401,
    contentType: "application/json",
    body: JSON.stringify(user
      ? { data: { user }, csrfToken: "test-csrf" }
      : { error: { code: "AUTH_REQUIRED", message: "Cần đăng nhập." } })
  }));
  await context.route("**/api/assembly-sessions?*", (route) => route.fulfill({
    status: listStatus,
    contentType: "application/json",
    body: JSON.stringify(listStatus === 200
      ? { data: items, meta: { page: 1, pageSize: 1, total: items.length } }
      : { error: { code: "SERVICE_UNAVAILABLE", message: "Tạm thời không tải được." } })
  }));
  await context.route("**/api/assembly-sessions/42", (route) => route.fulfill({
    contentType: "application/json",
    body: JSON.stringify({ data: { ...session, components: [], steps: [], assembledPartIds: [] } })
  }));
}

(async () => {
  const app = createApp({ config: loadConfig({ NODE_ENV: "test" }), database: null, logger: { info() {}, error() {} } });
  const server = http.createServer(app);
  await new Promise((resolve) => server.listen(0, "127.0.0.1", resolve));
  const base = `http://127.0.0.1:${server.address().port}`;
  const browser = await chromium.launch({ channel: "chrome", headless: true });
  try {
    const guest = await browser.newContext();
    await mockSessionApi(guest, {});
    const guestPage = await guest.newPage();
    guestPage.setDefaultTimeout(3000);
    await guestPage.goto(base);
    await guestPage.locator("#home-progress").getByText("Khám phá 3 mô hình robot").waitFor();
    assert.equal(await guestPage.locator('.nav-list a[href="pages/tai-khoan.html"] span').innerText(), "Tài khoản");
    assert.equal(await guestPage.locator("#home-progress a").first().getAttribute("href"), "pages/mau-robot.html");

    const signedIn = await browser.newContext();
    await mockSessionApi(signedIn, { user: { id: "7", fullName: "Huỳnh Minh Tài", role: "USER" }, items: [session] });
    const page = await signedIn.newPage();
    page.setDefaultTimeout(3000);
    const errors = [];
    page.on("pageerror", (error) => errors.push(error.message));
    await page.goto(base);
    await page.locator('.nav-list a[data-authenticated="true"]').waitFor();
    assert.equal(await page.locator('.nav-list a[href="pages/tai-khoan.html"] span').innerText(), "Chào, Tài");
    await page.locator('#home-progress a[href*="session=42"]').waitFor();
    assert.match(await page.locator("#home-progress").innerText(), /75%/);
    assert.match(await page.locator("#home-progress").innerText(), /2\/5 bước/);
    assert.match(await page.locator("#home-progress a").first().getAttribute("href"), /pages\/lap-rap-3d\.html\?model=line-follower&session=42/);
    for (const width of [320, 1440]) {
      await page.setViewportSize({ width, height: 900 });
      assert.equal(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth + 1), true, `${width}px horizontal overflow`);
      if (process.env.ARTIFACT_DIR) {
        fs.mkdirSync(process.env.ARTIFACT_DIR, { recursive: true });
        await page.screenshot({ path: path.join(process.env.ARTIFACT_DIR, `phase2-home-${width}.png`) });
        await page.locator("#home-progress").scrollIntoViewIfNeeded();
        await page.screenshot({ path: path.join(process.env.ARTIFACT_DIR, `phase2-progress-${width}.png`) });
      }
    }
    await page.locator('#home-progress a[href*="session=42"]').click();
    await page.locator("#assembly-3d-sync-status").getByText(/đã được đồng bộ/).waitFor();
    assert.match(page.url(), /session=42/);
    for (const file of ["mau-robot.html", "linh-kien.html", "lap-rap.html", "thu-vien.html", "thanh-vien.html"]) {
      await page.goto(`${base}/pages/${file}`);
      await page.locator('.nav-list a[data-authenticated="true"]').waitFor();
      assert.equal(await page.locator('.nav-list a[href$="tai-khoan.html"] span').innerText(), "Chào, Tài");
    }
    assert.deepEqual(errors, []);

    const noHistory = await browser.newContext();
    await mockSessionApi(noHistory, { user: { id: "8", fullName: "Thảo Nhi", role: "USER" }, items: [] });
    const newUserPage = await noHistory.newPage();
    newUserPage.setDefaultTimeout(3000);
    await newUserPage.goto(base);
    await newUserPage.locator("#home-progress").getByText(/chưa có phiên thực hành/).waitFor();
    assert.equal(await newUserPage.locator("#home-progress a").first().getAttribute("href"), "pages/mau-robot.html");

    const unavailable = await browser.newContext();
    await mockSessionApi(unavailable, { user: { id: "9", fullName: "Người học", role: "USER" }, listStatus: 503 });
    const unavailablePage = await unavailable.newPage();
    unavailablePage.setDefaultTimeout(3000);
    await unavailablePage.goto(base);
    await unavailablePage.locator("#home-progress").getByText(/Không thể tải tiến độ/).waitFor();
    assert.equal(await unavailablePage.locator("#home-progress a").first().getAttribute("href"), "pages/tai-khoan.html");
    console.log("PASS: guest CTA, shared signed-in navigation, recent-session widget, empty history.");
  } finally {
    await browser.close();
    await new Promise((resolve, reject) => server.close((error) => error ? reject(error) : resolve()));
  }
})().catch((error) => { console.error(error); process.exitCode = 1; });
