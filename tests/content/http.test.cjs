'use strict';
const { test } = require('node:test');
const assert = require('node:assert/strict');
const express = require('../../server/content/node_modules/express');
const { createContentRouter } = require('../../server/content/index.cjs');
test('HTTP CRUD contract, auth, CSRF, validation and safe errors', async t => {
  const app = express(); app.use(express.json());
  app.use((req, res, next) => { req.requestId = 'test-request'; next(); });
  let writes = 0;
  const repository = {
    list: async () => ({ data: [], meta: { page: 1, limit: 20, total: 0 } }),
    get: async () => { throw Object.assign(Error('missing'), { status: 404, code: 'NOT_FOUND' }); },
    create: async (kind, body) => { writes++; if (body.id === 'duplicate') throw Object.assign(Error('private SQL'), { code: 'ER_DUP_ENTRY' }); return body; },
    update: async (kind, id, body) => ({ id, ...body }), remove: async () => {},
    setPart: async (id, data) => data, removePart: async () => {}, parts: async () => []
  };
  app.use('/api', createContentRouter({ Router: express.Router, repository,
    requireAdmin: (req, res, next) => req.get('X-Test-Role') === 'admin' ? next() : res.status(req.get('X-Test-Role') ? 403 : 401).json({ error: { code: 'AUTH' } }),
    protectMutation: (req, res, next) => req.get('X-Test-CSRF') === 'valid' ? next() : res.status(403).json({ error: { code: 'CSRF' } })
  }));
  app.use((err, req, res, next) => res.status(500).json({ error: { code: 'INTERNAL_ERROR' } }));
  const server = app.listen(0, '127.0.0.1'); await new Promise(resolve => server.once('listening', resolve));
  t.after(() => new Promise(resolve => server.close(resolve)));
  const call = (url, method = 'GET', body, headers = {}) => fetch(`http://127.0.0.1:${server.address().port}/api${url}`, { method, headers: { 'Content-Type': 'application/json', ...headers }, ...(body ? { body: JSON.stringify(body) } : {}) });
  const admin = { 'X-Test-Role': 'admin', 'X-Test-CSRF': 'valid' };
  assert.equal((await call('/robots')).status, 200);
  assert.equal((await call('/robots/missing')).status, 404);
  const invalid = await call('/robots?limit=101');
  assert.equal(invalid.status, 422);
  assert.equal((await invalid.json()).error.requestId, 'test-request');
  assert.equal((await call('/admin/robots')).status, 401);
  assert.equal((await call('/admin/robots', 'GET', null, { 'X-Test-Role': 'user' })).status, 403);
  const body = require('./seed-fixture.json').components[0];
  assert.equal((await call('/admin/components', 'POST', body, { 'X-Test-Role': 'admin' })).status, 403);
  assert.equal(writes, 0);
  assert.equal((await call('/admin/components', 'POST', { ...body, role: 'admin' }, admin)).status, 422);
  assert.equal((await call('/admin/components', 'POST', body, admin)).status, 201);
  const duplicate = await call('/admin/components', 'POST', { ...body, id: 'duplicate' }, admin);
  assert.equal(duplicate.status, 409); assert.ok(!(await duplicate.text()).includes('private SQL'));
  assert.equal((await call('/admin/components/sg90', 'PATCH', { name: 'Servo' }, admin)).status, 200);
  assert.equal((await call('/admin/components/sg90', 'DELETE', null, admin)).status, 204);
  assert.equal((await call('/admin/robots/mini-arm/components/sg90', 'PUT', { quantity: 4 }, admin)).status, 200);
  assert.equal((await call('/admin/robots/mini-arm/components/sg90', 'PUT', { quantity: 0 }, admin)).status, 422);
});
