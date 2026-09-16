'use strict';
// Optional real-browser verification against MySQL. See docs/content/DEMO.md.
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { chromium } = require(process.env.CONTENT_PLAYWRIGHT_PATH || 'playwright');
const { startDemo } = require('./demo-server.cjs');
const fixture = require('./seed-fixture.json');

(async () => {
  const demo = await startDemo();
  let browser;
  try {
    browser = await chromium.launch({ headless: true, ...(process.env.CONTENT_BROWSER_CHANNEL ? { channel: process.env.CONTENT_BROWSER_CHANNEL } : {}) });
    const page = await browser.newPage();
    const errors = [];
    page.on('pageerror', error => errors.push(error.message));
    page.on('dialog', dialog => dialog.accept());
    await page.goto(demo.entryUrl);
    await page.waitForFunction(() => !document.getElementById('workspace').disabled);
    const choose = async kind => {
      await page.selectOption('#kind', kind);
      await page.waitForFunction(() => !document.getElementById('workspace').disabled);
      await page.waitForFunction(() => document.getElementById('paging').textContent.includes('Trang'));
    };
    const submit = async () => {
      await page.click('#editor button[type=submit]');
      await page.waitForFunction(() => !document.getElementById('workspace').disabled);
      assert.equal(await page.locator('#status').textContent(), 'Đã lưu nội dung.');
    };
    for (const [kind, source] of [['robots', fixture.robots[0]], ['components', fixture.components[0]], ['steps', fixture.steps[0]], ['library-resources', fixture.library[0]]]) {
      await choose(kind);
      await page.click('#new');
      const row = { ...source, id: `browser-${kind}` };
      if (kind === 'steps') row.stepOrder = 9998;
      if (kind === 'library-resources') row.robotId = null;
      for (const [key, value] of Object.entries(row)) {
        const field = page.locator(`#field-${key}`);
        if (['level', 'type'].includes(key)) await field.selectOption(value);
        else await field.fill(value === null ? '' : typeof value === 'object' ? JSON.stringify(value) : String(value));
      }
      await submit();
      const textKey = ['robots', 'components'].includes(kind) ? 'name' : 'title';
      await page.fill(`#field-${textKey}`, `Updated ${kind}`);
      await submit();
      assert.equal((await (await page.request.get(`${demo.origin}/api/${kind}/${row.id}`)).json()).data[textKey], `Updated ${kind}`);
      if (kind === 'robots') {
        await page.fill('#component-id', 'sg90');
        await page.fill('#quantity', '2');
        await page.click('#part-form button');
        await page.waitForFunction(() => document.getElementById('parts').textContent.includes('sg90'));
        await page.click('#delete');
        await page.waitForFunction(() => document.getElementById('status').dataset.error === 'true');
        assert.equal((await page.request.get(`${demo.origin}/api/robots/${row.id}`)).status(), 200);
        await page.click('#parts button');
        await page.waitForFunction(() => !document.getElementById('workspace').disabled && !document.getElementById('parts').children.length);
      }
      await page.click('#delete');
      await page.waitForFunction(() => document.getElementById('status').textContent === 'Đã xóa nội dung.');
      assert.equal((await page.request.get(`${demo.origin}/api/${kind}/${row.id}`)).status(), 404);
    }
    console.log('PASS browser CRUD: robots, components, steps, library; relation add/remove and FK rejection.');

    await choose('robots');
    await page.getByRole('button', { name: fixture.robots.find(r => r.id === 'mini-arm').name, exact: true }).click();
    await page.waitForFunction(() => document.getElementById('parts').children.length > 0);
    let release;
    const held = new Promise(resolve => { release = resolve; });
    await page.route('**/api/robots/line-follower/components', async route => {
      await held;
      await route.fulfill({ status: 500, json: { error: { message: 'Expected load failure' } } });
    });
    await page.getByRole('button', { name: fixture.robots.find(r => r.id === 'line-follower').name, exact: true }).click();
    assert.equal(await page.locator('#parts button').count(), 0, 'Old robot remove buttons must disappear immediately');
    release();
    await page.waitForFunction(() => document.getElementById('status').textContent === 'Expected load failure');
    assert.equal(await page.locator('#parts button').count(), 0);
    await page.unroute('**/api/robots/line-follower/components');
    await page.route('**/api/admin/components?*', route => route.fulfill({ status: 500, json: { error: { message: 'Expected list failure' } } }));
    await page.selectOption('#kind', 'components');
    await page.waitForFunction(() => document.getElementById('status').textContent === 'Expected list failure');
    assert.equal(await page.locator('#items button').count(), 0, 'Old category rows must not remain clickable');
    await page.unroute('**/api/admin/components?*');
    console.log('PASS stale robot/category lists cleared, including failed requests.');

    await choose('components');
    const session = await (await page.request.get(`${demo.origin}/api/auth/me`)).json();
    assert.equal((await fetch(`${demo.origin}/api/admin/robots`)).status, 401);
    assert.equal((await page.request.post(`${demo.origin}/api/admin/components`, { data: fixture.components[0] })).status(), 403);
    const headers = { 'X-CSRF-Token': session.csrfToken };
    const invalid = await page.request.post(`${demo.origin}/api/admin/components`, { headers, data: { ...fixture.components[0], id: 'bad-row', unexpected: true } });
    assert.equal(invalid.status(), 422);
    assert.ok((await invalid.json()).error.requestId);
    assert.equal((await page.request.post(`${demo.origin}/api/admin/components`, { headers, data: fixture.components[0] })).status(), 409);
    assert.equal((await page.request.post(`${demo.origin}/api/admin/components`, { headers: { ...headers, 'Content-Type': 'application/json' }, data: '{' })).status(), 400);
    assert.equal((await page.request.post(`${demo.origin}/api/admin/components`, { headers, data: { text: 'x'.repeat(270000) } })).status(), 413);
    for (const privatePath of ['/server/content/.env', '/database/schema.sql', '/docs/content/API.md']) assert.equal((await page.request.get(`${demo.origin}${privatePath}`)).status(), 404);
    console.log('PASS HTTP+MySQL: 401, CSRF 403, validation 422, conflict 409, JSON 400/413; private files not served.');

    // Deleting the only item on page 2 must return to page 1.
    let lastDeleted = false;
    await page.route('**/api/admin/components?*', route => {
      const currentPage = Number(new URL(route.request().url()).searchParams.get('page'));
      const rows = currentPage === 2 ? (lastDeleted ? [] : [{ ...fixture.components[0], id: 'last-page-item' }]) : fixture.components;
      return route.fulfill({ json: { data: rows, meta: { page: currentPage, limit: 20, total: lastDeleted ? 20 : 21 } } });
    });
    await page.route('**/api/admin/components/last-page-item', route => { lastDeleted = true; return route.fulfill({ status: 204 }); });
    await choose('components');
    await page.click('#next');
    await page.waitForFunction(() => document.getElementById('paging').textContent.startsWith('Trang 2'));
    await page.click('#items button');
    await page.click('#delete');
    await page.waitForFunction(() => document.getElementById('status').textContent === 'Đã xóa nội dung.');
    assert.match(await page.locator('#paging').textContent(), /^Trang 1/);
    await page.unroute('**/api/admin/components?*');
    await page.unroute('**/api/admin/components/last-page-item');
    console.log('PASS pagination recovery after deleting the last row.');

    await choose('robots');
    await page.getByRole('button', { name: fixture.robots[0].name, exact: true }).click();
    await page.waitForFunction(() => document.getElementById('parts').children.length > 0);
    const artifacts = path.join(__dirname, 'artifacts');
    fs.mkdirSync(artifacts, { recursive: true });
    for (const width of [375, 768, 1024, 1440]) {
      await page.setViewportSize({ width, height: 900 });
      assert.equal(await page.evaluate(() => document.documentElement.scrollWidth <= innerWidth), true, `Overflow at ${width}px`);
      await page.screenshot({ path: path.join(artifacts, `admin-${width}.png`), fullPage: true });
    }
    assert.deepEqual(errors, []);
    console.log('PASS responsive 375/768/1024/1440px, screenshots saved; no browser exceptions.');
  } finally {
    if (browser) await browser.close();
    await demo.stop();
  }
})().catch(error => { console.error(error); process.exitCode = 1; });
