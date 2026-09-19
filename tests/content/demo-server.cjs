'use strict';
// Local content demo only. All writes share one transaction and are rolled back.
const path = require('node:path');
const crypto = require('node:crypto');
const express = require('../../server/content/node_modules/express');
const mysql = require('../../server/content/node_modules/mysql2/promise');
const { createContentRouter } = require('../../server/content/index.cjs');

async function startDemo({ port = Number(process.env.CONTENT_DEMO_PORT || 0) } = {}) {
  if (process.env.NODE_ENV === 'production') throw Error('Demo cannot run in production.');
  if (!process.env.CONTENT_TEST_DB_NAME?.endsWith('_test')) throw Error('Use a separately seeded database ending in _test.');
  const connection = await mysql.createConnection({
    host: process.env.CONTENT_TEST_DB_HOST || '127.0.0.1',
    port: Number(process.env.CONTENT_TEST_DB_PORT || 3306),
    user: process.env.CONTENT_TEST_DB_USER,
    password: process.env.CONTENT_TEST_DB_PASSWORD,
    database: process.env.CONTENT_TEST_DB_NAME,
    timezone: 'Z', supportBigNumbers: true, bigNumberStrings: true
  });
  let server;
  try {
    await connection.execute("SELECT version FROM schema_migrations WHERE version = '003_session_visual_parts'").then(([rows]) => {
      if (!rows.length) throw Error('Apply migration 003_session_visual_parts before demo.');
    });
    await connection.beginTransaction();
    const app = express();
    const key = crypto.randomBytes(32).toString('hex');
    const session = crypto.randomBytes(32).toString('hex');
    const csrfToken = crypto.randomBytes(32).toString('hex');
    const root = path.resolve(__dirname, '../..');
    let origin;
    const deny = (res, status, code) => res.status(status).json({ error: { code, message: code } });
    app.disable('x-powered-by');
    app.use((req, res, next) => {
      if (req.get('host') !== new URL(origin).host) return deny(res, 403, 'FORBIDDEN');
      req.requestId = crypto.randomUUID();
      res.set({ 'Cache-Control': 'no-store', 'Referrer-Policy': 'no-referrer', 'X-Content-Type-Options': 'nosniff' });
      next();
    });
    app.use(express.json({ limit: '256kb' }));
    const requireAdmin = (req, res, next) => {
      if (!req.headers.cookie?.split(';').some(cookie => cookie.trim() === `content_demo=${session}`)) return deny(res, 401, 'AUTH_REQUIRED');
      next();
    };
    const protectMutation = (req, res, next) => {
      if (req.get('X-CSRF-Token') !== csrfToken || (req.get('Origin') && req.get('Origin') !== origin)) return deny(res, 403, 'FORBIDDEN');
      next();
    };
    app.get('/__demo/start', (req, res) => {
      if (req.query.key !== key) return deny(res, 403, 'FORBIDDEN');
      res.cookie('content_demo', session, { httpOnly: true, sameSite: 'strict', path: '/api' });
      res.redirect('/pages/admin-content.html');
    });
    app.get('/api/auth/me', requireAdmin, (req, res) => res.json({ data: { user: { id: 'demo-only', fullName: 'Content demo', role: 'ADMIN' } }, csrfToken }));
    app.use('/api', createContentRouter({ Router: express.Router, pool: connection, requireAdmin, protectMutation }));
    app.get(['/', '/index.html'], (req, res) => res.sendFile(path.join(root, 'index.html')));
    app.use('/pages', express.static(path.join(root, 'pages'), { dotfiles: 'deny', index: false }));
    app.use('/assets', express.static(path.join(root, 'assets'), { dotfiles: 'deny', index: false }));
    app.use((req, res) => deny(res, 404, 'NOT_FOUND'));
    app.use((err, req, res, next) => {
      if (res.headersSent) return next(err);
      if (err.type === 'entity.parse.failed') return deny(res, 400, 'INVALID_JSON');
      if (err.type === 'entity.too.large') return deny(res, 413, 'PAYLOAD_TOO_LARGE');
      return deny(res, 500, 'INTERNAL_SERVER_ERROR');
    });
    server = await new Promise((resolve, reject) => {
      const listener = app.listen(port, '127.0.0.1', () => resolve(listener));
      listener.once('error', reject);
    });
    origin = `http://127.0.0.1:${server.address().port}`;
    let stopping;
    const stop = () => stopping ||= (async () => {
      await new Promise(resolve => server.close(resolve));
      try { await connection.rollback(); } finally { await connection.end(); }
    })();
    return { origin, entryUrl: `${origin}/__demo/start?key=${key}`, stop };
  } catch (err) {
    if (server) server.close();
    await connection.end();
    throw err;
  }
}

if (require.main === module) {
  startDemo().then(demo => {
    console.log('LOCAL DEMO: MySQL content API; demo admin fixture, not real login. Writes roll back on exit.');
    console.log(demo.entryUrl);
    for (const signal of ['SIGINT', 'SIGTERM']) process.once(signal, () => demo.stop().then(() => process.exit(0)));
  }).catch(err => { console.error('Demo failed:', err.code || err.message); process.exitCode = 1; });
}
module.exports = { startDemo };
