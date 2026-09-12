/* Run against a local static server with BASE_URL and Playwright in NODE_PATH. */
const {chromium} = require('playwright');
const assert = require('node:assert/strict');
const base = process.env.BASE_URL || 'http://127.0.0.1:4176';

(async () => {
  const browser = await chromium.launch({channel:'chrome'});
  try {
    const page = await browser.newPage({viewport:{width:1440,height:1000}});
    await page.clock.install();
    await page.goto(base);
    const root = page.locator('#featured-robots');
    await root.scrollIntoViewIfNeeded();
    await page.waitForTimeout(200);
    await root.locator('.explorer-play').click();
    await page.clock.fastForward(6500);
    await page.waitForFunction(() => document.querySelector('#featured-robots .explorer-count').textContent.startsWith('02'));
    await root.locator('.explorer-thumb').nth(2).focus();
    await page.clock.fastForward(13000);
    assert.ok((await root.locator('.explorer-count').textContent()).startsWith('02'), 'Focus pauses autoplay');
    await page.keyboard.press('Enter');
    await page.waitForFunction(() => document.querySelector('#featured-robots .explorer-count').textContent.startsWith('03'));
    assert.equal(await root.locator('.explorer-play').getAttribute('aria-pressed'), 'false', 'Manual selection stops autoplay');
    await page.emulateMedia({reducedMotion:'reduce'});
    await page.waitForFunction(() => document.querySelector('#featured-robots .explorer-play').disabled);
    assert.ok(await root.locator('.explorer-play').isDisabled());
    for (const [id,count] of [['featured-robots',5],['component-explorer',6]]) {
      const stage = page.locator('#'+id);
      assert.equal(await stage.locator('.explorer-thumb').count(),count);
      await stage.locator('.explorer-thumb').first().focus();
      await page.keyboard.press('End');
      await page.waitForFunction(({id,count}) => document.querySelector('#'+id+' .explorer-count').textContent.startsWith(String(count).padStart(2,'0')), {id,count});
      assert.match(await stage.locator('.explorer-link').getAttribute('href'), /thu-vien.html#reference-/);
      await stage.locator('.explorer-controls button').nth(1).click();
      await page.waitForFunction(id => document.querySelector('#'+id+' .explorer-count').textContent.startsWith('01'), id);
    }
    const phone = await browser.newPage({viewport:{width:390,height:844},isMobile:true,hasTouch:true,reducedMotion:'reduce'});
    await phone.goto(base);
    const stage = phone.locator('#featured-robots');
    await stage.scrollIntoViewIfNeeded();
    const bounds = await stage.boundingBox();
    const cdp = await phone.context().newCDPSession(phone);
    const y = Math.max(100, bounds.y + 100);
    await cdp.send('Input.dispatchTouchEvent',{type:'touchStart',touchPoints:[{x:300,y}]});
    await cdp.send('Input.dispatchTouchEvent',{type:'touchMove',touchPoints:[{x:90,y}]});
    await cdp.send('Input.dispatchTouchEvent',{type:'touchEnd',touchPoints:[]});
    await phone.waitForFunction(() => document.querySelector('#featured-robots .explorer-count').textContent.startsWith('02'));
    assert.ok(await phone.evaluate(() => document.documentElement.scrollWidth <= innerWidth));
    const failure = await browser.newPage({viewport:{width:1440,height:1000},reducedMotion:'reduce'});
    await failure.route('**/dobot-arm.webp', route => route.abort());
    await failure.goto(base);
    await failure.locator('#featured-robots .explorer-thumb').last().click();
    await failure.waitForFunction(() => document.querySelector('#featured-robots [role=status]').textContent.includes('chưa tải được'));
    assert.equal(await failure.locator('#featured-robots h3').textContent(), 'Robot dò đường', 'Failed image preserves current slide');
    console.log('PASS: autoplay, focus pause, manual stop, reduced motion, keyboard, wraparound, touch swipe, failed-image fallback.');
  } finally { await browser.close(); }
})().catch(error => {console.error(error); process.exitCode=1;});
