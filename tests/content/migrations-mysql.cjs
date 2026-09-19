'use strict';
const fs = require('node:fs');
const path = require('node:path');
const assert = require('node:assert/strict');
const mysql = require('../../server/content/node_modules/mysql2/promise');
(async () => {
  if (!process.env.CONTENT_TEST_DB_NAME?.endsWith('_test')) throw Error('Test configuration required.');
  const c = await mysql.createConnection({ host: process.env.CONTENT_TEST_DB_HOST || '127.0.0.1', port: Number(process.env.CONTENT_TEST_DB_PORT || 3306), user: process.env.CONTENT_TEST_DB_USER, password: process.env.CONTENT_TEST_DB_PASSWORD, multipleStatements: true });
  const name = `content_schema_${process.pid}_${Date.now()}_test`;
  assert.match(name, /^content_schema_\d+_\d+_test$/);
  const sql = file => fs.readFileSync(path.resolve(__dirname, '../../database', file), 'utf8');
  let created = false;
  try {
    await c.query(`CREATE DATABASE \`${name}\` CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci`);
    created = true;
    await c.query(`USE \`${name}\``);
    await c.query(sql('migrations/001_initial.sql'));
    await c.query(sql('seed.sql'));
    await c.execute("INSERT INTO users(email,display_name,password_hash) VALUES ('migration@example.invalid','Migration','not-a-login-hash')");
    await c.execute("INSERT INTO assembly_sessions(user_id,robot_id,status) VALUES (1,'mini-arm','in_progress'),(1,'mini-arm','completed')");
    await c.execute("INSERT INTO session_steps(session_id,robot_id,step_id,completed) VALUES (1,'mini-arm','mini-arm-step-1',1)");
    await c.query(sql('migrations/002_backend_contract.sql'));
    const [rows] = await c.execute('SELECT status FROM assembly_sessions ORDER BY id');
    assert.deepEqual(rows.map(row => row.status), ['IN_PROGRESS', 'COMPLETED']);
    const [[progress]] = await c.execute('SELECT completed, updated_at FROM session_steps WHERE session_id = 1');
    assert.equal(progress.completed, 1); assert.ok(progress.updated_at);
    const [upgraded] = await c.query('SHOW TABLES');
    assert.equal(upgraded.length, 11);
    await c.query(sql('migrations/003_session_visual_parts.sql'));
    const [latest] = await c.query('SHOW TABLES');
    assert.equal(latest.length, 12);
    const [[visual]] = await c.execute('SELECT COUNT(*) AS total FROM session_visual_parts');
    assert.equal(visual.total, 0);
    console.log('PASS upgrade 001 -> 002 -> 003 preserves legacy sessions and progress (12 tables).');
  } finally {
    try { if (created) await c.query(`DROP DATABASE \`${name}\``); }
    finally { await c.end(); }
  }
})().catch(err => { console.error('Migration check failed:', err.code || err.message); process.exitCode = 1; });
