"use strict";

/* Đợt 3: quản trị nội dung, chú thích nổi và hiệu ứng hỗ trợ.
   API được giả lập bằng route interception nên test không cần MySQL và không
   dùng tài khoản thật: vai trò trả về được đổi ngay trong từng ngữ cảnh. */

const assert = require("node:assert/strict");
const http = require("node:http");
const { chromium } = require("playwright-core");
const { createApp } = require("../server/app");
const { loadConfig } = require("../server/config/env");

const CSRF = "phase3-csrf";
const PAGE_SIZE = 20;
const KINDS = "robots|components|steps|library-resources";
const KIND_LIST = new RegExp(`^/admin/(?:${KINDS})$`);
const KIND_ITEM = new RegExp(`^/admin/(?:${KINDS})/[\\w-]+$`);

function makeComponents(count) {
  return Array.from({ length: count }, (unused, index) => ({
    id: `component-${index + 1}`,
    name: `Linh kiện ${index + 1}`,
    category: "Kiểm thử",
    image: "/assets/images/components/l298n.png",
    description: `Mô tả ${index + 1}`,
    specs: { "Ghi chú": `số ${index + 1}` }
  }));
}

/* session: null nghĩa là chưa đăng nhập; listDelayMs giữ danh sách trong trạng
   thái đang tải để kiểm tra khung xương. */
function createRouter(options) {
  const components = makeComponents(24);
  const calls = { patches: [], logouts: 0 };

  async function respond(route) {
    const request = route.request();
    const url = new URL(request.url());
    const pathName = url.pathname.slice(4);
    let status = 200;
    let payload;

    if (pathName === "/auth/me") {
      if (!options.session) {
        status = 401;
        payload = { error: { code: "AUTH_REQUIRED", message: "Bạn cần đăng nhập để tiếp tục." } };
      } else {
        payload = { data: { user: options.session }, csrfToken: CSRF };
      }
    } else if (pathName === "/auth/logout") {
      calls.logouts += 1;
      await route.fulfill({ status: 204, body: "" });
      return;
    } else if (KIND_LIST.test(pathName) && request.method() === "GET") {
      if (options.listDelayMs) await new Promise((resolve) => setTimeout(resolve, options.listDelayMs));
      const page = Number(url.searchParams.get("page") || 1);
      payload = {
        data: components.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE),
        meta: { page, pageSize: PAGE_SIZE, total: components.length }
      };
    } else if (KIND_ITEM.test(pathName) && request.method() === "PATCH") {
      assert.equal(request.headers()["x-csrf-token"], CSRF, "PATCH quản trị phải kèm mã CSRF");
      const id = pathName.split("/").pop();
      const body = request.postDataJSON();
      calls.patches.push({ id, body });
      const record = components.find((item) => item.id === id);
      Object.assign(record, body);
      payload = { data: record };
    } else {
      status = 404;
      payload = { error: { code: "NOT_FOUND", message: pathName } };
    }

    await route.fulfill({ status, contentType: "application/json", body: JSON.stringify(payload) });
  }

  return { respond, calls, components };
}

async function openAdmin(browser, base, options, contextOptions = {}) {
  const router = createRouter(options);
  const context = await browser.newContext(contextOptions);
  await context.route("**/api/**", router.respond);
  const page = await context.newPage();
  const errors = [];
  page.on("pageerror", (error) => errors.push(error.message));
  await page.goto(`${base}/pages/admin-content.html`);
  return { page, context, router, errors };
}

(async () => {
  const app = createApp({ config: loadConfig({ NODE_ENV: "test" }), database: null, logger: { info() {}, error() {} } });
  const server = http.createServer(app);
  await new Promise((resolve) => server.listen(0, "127.0.0.1", resolve));
  const base = `http://127.0.0.1:${server.address().port}`;
  const browser = await chromium.launch({ channel: "chrome", headless: true });
  const admin = { id: "6", fullName: "Quản trị nội dung", email: "admin@robotlab.local", role: "ADMIN" };
  const checks = [];

  try {
    /* 1. Chưa đăng nhập: chỉ thấy cổng vào, không thấy vùng làm việc. */
    {
      const { page, context, errors } = await openAdmin(browser, base, { session: null });
      await page.locator("#admin-gate:not([hidden])").waitFor();
      assert.match(await page.locator("#gate-message").innerText(), /đăng nhập bằng tài khoản quản trị/i);
      assert.equal(await page.locator("#workspace").isHidden(), true, "Khách không được thấy vùng quản trị");
      assert.equal(await page.locator('#admin-gate a[href="tai-khoan.html"]').count(), 1, "Cổng phải có lối đăng nhập");
      // Không được lặp cùng một câu ở cả dòng trạng thái lẫn khối cổng.
      assert.equal(await page.locator("#status").isHidden(), true, "Dòng trạng thái phải ẩn khi đã có khối cổng");
      assert.deepEqual(errors, []);
      await context.close();
      checks.push("khách bị chặn khỏi khu vực quản trị");
    }

    /* 2. Đăng nhập nhưng không phải ADMIN: vẫn bị chặn và được nói rõ lý do. */
    {
      const { page, context, errors } = await openAdmin(browser, base, {
        session: { id: "7", fullName: "Sinh viên", email: "sv@example.invalid", role: "USER" }
      });
      await page.locator("#admin-gate:not([hidden])").waitFor();
      assert.match(await page.locator("#gate-message").innerText(), /không có quyền quản trị/i);
      assert.match(await page.locator("#gate-message").innerText(), /sv@example\.invalid/);
      assert.equal(await page.locator("#workspace").isHidden(), true);
      assert.deepEqual(errors, []);
      await context.close();
      checks.push("tài khoản USER bị từ chối kèm lý do");
    }

    /* 3. ADMIN: vào được, thấy danh sách, phân trang và danh tính phiên. */
    {
      const { page, context, router, errors } = await openAdmin(browser, base, { session: admin });
      await page.locator("#workspace:not([hidden])").waitFor();
      assert.equal(await page.locator("#admin-gate").isHidden(), true);
      assert.equal(await page.locator("#admin-email").innerText(), admin.email);
      assert.equal(await page.locator('.status-badge[data-state="COMPLETED"]').innerText(), "ADMIN");
      await page.locator("#items li button").first().waitFor();
      assert.equal(await page.locator("#items li button").count(), PAGE_SIZE);
      assert.match(await page.locator("#paging").innerText(), /Trang 1 · 24 mục/);

      await page.locator("#next").click();
      await page.locator("#items li button").first().waitFor();
      assert.equal(await page.locator("#items li button").count(), 4, "Trang 2 còn 4 mục");
      assert.equal(await page.locator("#next").isDisabled(), true);
      await page.locator("#previous").click();
      await page.locator("#items li button").nth(19).waitFor();

      /* Đổi danh mục thì lối kiểm chứng công khai phải trỏ đúng trang. */
      await page.locator("#kind").selectOption("library-resources");
      assert.match(await page.locator("#public-view").getAttribute("href"), /thu-vien\.html$/);
      await page.locator("#kind").selectOption("components");
      assert.match(await page.locator("#public-view").getAttribute("href"), /linh-kien\.html$/);
      await page.waitForFunction((size) => document.querySelectorAll("#items li button").length === size, PAGE_SIZE);

      /* 4. Lưu hợp lệ: gửi kèm CSRF và báo thành công. */
      await page.locator("#items li button").first().click();
      assert.equal(await page.locator("#items li button").first().getAttribute("aria-current"), "true");
      await page.locator("#field-name").fill("Linh kiện đã đổi tên");
      await page.locator("#editor button[type=submit]").click();
      await page.locator('#status[data-state="saved"]').waitFor();
      assert.equal(router.calls.patches.length, 1);
      assert.equal(router.calls.patches[0].body.name, "Linh kiện đã đổi tên");

      /* 5. JSON hỏng phải bị chặn ngay tại chỗ, không gửi request nào thêm. */
      await page.locator("#field-specs").fill("{ hỏng");
      await page.locator("#editor button[type=submit]").click();
      await page.locator('#status[data-state="error"]').waitFor();
      assert.equal(await page.locator("#field-specs").getAttribute("aria-invalid"), "true");
      assert.match(await page.locator("#field-specs-error").innerText(), /JSON không hợp lệ/);
      assert.equal(router.calls.patches.length, 1, "Không được gửi PATCH khi JSON còn hỏng");

      /* 6. Chú thích nổi: bổ sung thông tin, không thay nhãn nút. */
      const logout = page.locator("#admin-logout");
      assert.equal(await logout.getAttribute("aria-label"), "Đăng xuất khỏi tài khoản quản trị");
      await logout.hover();
      await page.locator("#ral-tooltip.is-visible").waitFor();
      assert.equal(await page.locator("#ral-tooltip").innerText(), "Đăng xuất khỏi tài khoản quản trị");
      assert.equal(await logout.getAttribute("aria-describedby"), "ral-tooltip");
      await page.keyboard.press("Escape");
      await page.locator("#ral-tooltip").waitFor({ state: "hidden" });
      assert.equal(await logout.getAttribute("aria-describedby"), null, "Đóng chú thích phải gỡ liên kết mô tả");

      /* 7. Trang quản trị là trang mới trong khung chung nên phải tự kiểm tra
         tràn ngang; bộ smoke hiện chỉ đi qua bảy trang công khai. */
      for (const width of [320, 768, 1440]) {
        await page.setViewportSize({ width, height: 900 });
        assert.equal(
          await page.evaluate(() => document.documentElement.scrollWidth <= window.innerWidth + 1),
          true,
          `Trang quản trị tràn ngang ở ${width}px`
        );
      }

      assert.deepEqual(errors, []);
      await context.close();
      checks.push("ADMIN duyệt, phân trang, lưu, chặn JSON hỏng, chú thích và bố cục 320–1440px");
    }

    /* 7. Khung xương hiện trong lúc chờ API, rồi nhường chỗ cho dữ liệu thật. */
    {
      const { page, context, errors } = await openAdmin(browser, base, { session: admin, listDelayMs: 900 });
      await page.locator("#items .skeleton").first().waitFor();
      assert.ok(await page.locator("#items .skeleton").count() > 0, "Phải có khung xương khi đang tải");
      assert.equal(await page.locator('#status[data-state="loading"]').count(), 1);
      await page.locator("#items li button").first().waitFor({ timeout: 5000 });
      assert.equal(await page.locator("#items .skeleton").count(), 0, "Khung xương phải biến mất khi có dữ liệu");
      assert.deepEqual(errors, []);
      await context.close();
      checks.push("khung xương xuất hiện rồi nhường chỗ cho dữ liệu");
    }

    /* 8. Giảm chuyển động: chú thích vẫn dùng được, chỉ bỏ phần chuyển cảnh. */
    {
      const { page, context, errors } = await openAdmin(browser, base, { session: admin }, { reducedMotion: "reduce" });
      await page.locator("#workspace:not([hidden])").waitFor();
      await page.locator("#admin-logout").hover();
      await page.locator("#ral-tooltip.is-visible").waitFor();
      const duration = await page.locator("#ral-tooltip").evaluate(
        (node) => getComputedStyle(node).transitionDuration
      );
      assert.equal(duration, "0s", "Giảm chuyển động phải tắt chuyển cảnh của chú thích");
      assert.deepEqual(errors, []);
      await context.close();
      checks.push("giảm chuyển động vẫn giữ nguyên chức năng chú thích");
    }

    console.log(`PASS đợt 3 (${checks.length} nhóm kiểm tra)`);
    checks.forEach((line) => console.log(` - ${line}`));
  } finally {
    await browser.close();
    await new Promise((resolve) => server.close(resolve));
  }
})().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
