'use strict';
const { test } = require('node:test');
const assert = require('node:assert/strict');
const fs = require('node:fs');
const path = require('node:path');
const { validate, identifier, pagination } = require('../../server/content/validation.cjs');
const { createRepository } = require('../../server/content/repository.cjs');
const { createContentRouter } = require('../../server/content/index.cjs');
const seed = require('./seed-fixture.json');
test('seed validates and contains every robot part and ordered step', () => {
  for (const [kind, rows] of Object.entries({ robots: seed.robots, components: seed.components, steps: seed.steps, 'library-resources': seed.library })) for (const row of rows) validate(kind, row);
  assert.equal(seed.robots.length, 3); assert.equal(seed.steps.length, 15);
  for (const part of seed.relations) {
    validate('relation', { componentId: part.componentId, quantity: part.quantity });
    assert.ok(seed.components.some(c => c.id === part.componentId));
    assert.ok(seed.robots.some(r => r.id === part.robotId));
  }
  for (const robot of seed.robots) assert.deepEqual(seed.steps.filter(s => s.robotId === robot.id).map(s => s.stepOrder), [1, 2, 3, 4, 5]);
  for (const row of [...seed.robots, ...seed.components, ...seed.library]) assert.ok(fs.existsSync(path.resolve(__dirname, '../..', (row.image || row.url).slice(1))));
});
test('schema snapshot equals ordered migrations', () => {
  const directory = path.resolve(__dirname, '../../database/migrations');
  const expected = fs.readdirSync(directory).filter(name => name.endsWith('.sql')).sort().map(name => fs.readFileSync(path.join(directory, name), 'utf8').replace(/\r\n/g, '\n').trimEnd()).join('\n\n') + '\n';
  assert.equal(fs.readFileSync(path.resolve(directory, '../schema.sql'), 'utf8').replace(/\r\n/g, '\n'), expected);
});
test('rejects malformed input, mass assignment, unsafe links and bad pagination', () => {
  for (const input of [null, [], {}, { unknown: true }, { id: 'replacement' }]) assert.throws(() => validate('robots', input, true));
  for (const id of ["x' OR 1=1", '../x', 'UPPER', 12, 'a'.repeat(65)]) assert.throws(() => identifier(id));
  for (const quantity of [0, -1, 1.5, '2', 10001]) assert.throws(() => validate('relation', { componentId: 'sg90', quantity }));
  for (const image of ['javascript:alert(1)', '//evil.test/x', '/assets/../secret', 'https://', 'https://u:p@host/x']) assert.throws(() => validate('robots', { image }, true));
  for (const value of ['0', '-1', '1 OR 1', ['2'], '101']) assert.throws(() => pagination({ limit: value }));
  assert.deepEqual(pagination({}), { page: 1, limit: 20 });
});
test('repository parameterizes writes and never forwards JSON as raw SQL', async () => {
  const calls = [];
  const repo = createRepository({ execute: async (sql, args) => { calls.push({ sql, args }); return sql.startsWith('SELECT') ? [[{ id: 'sg90' }]] : [{ affectedRows: 1 }]; } });
  const name = "x'); DROP TABLE users; --";
  await repo.update('components', 'sg90', { name, specs: { voltage: '5V' } });
  assert.ok(!calls[0].sql.includes(name)); assert.deepEqual(calls[0].args, [name, '{"voltage":"5V"}', 'sg90']);
});
test('repository reports missing deletions', async () => {
  const repo = createRepository({ execute: async () => [{ affectedRows: 0 }] });
  await assert.rejects(repo.remove('robots', 'missing'), { status: 404 });
});
test('integration refuses missing security middleware', () => assert.throws(() => createContentRouter({ Router() {} }), /requireAdmin/));
