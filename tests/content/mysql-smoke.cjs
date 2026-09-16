'use strict';
// Run manually against a separately provisioned, seeded test DB. No schema creation/drop.
// Env: CONTENT_TEST_DB_HOST, CONTENT_TEST_DB_USER, CONTENT_TEST_DB_PASSWORD, CONTENT_TEST_DB_NAME.
const mysql = require('../../server/content/node_modules/mysql2/promise');
const assert = require('node:assert/strict');
const { createRepository } = require('../../server/content/repository.cjs');
(async () => {
  if (!process.env.CONTENT_TEST_DB_NAME?.endsWith('_test')) throw Error('CONTENT_TEST_DB_NAME phải kết thúc bằng _test; chỉ dùng DB kiểm thử riêng.');
  const connection = await mysql.createConnection({ host: process.env.CONTENT_TEST_DB_HOST || '127.0.0.1', port: Number(process.env.CONTENT_TEST_DB_PORT || 3306), user: process.env.CONTENT_TEST_DB_USER, password: process.env.CONTENT_TEST_DB_PASSWORD, database: process.env.CONTENT_TEST_DB_NAME });
  try {
    await connection.beginTransaction();
    const repo = createRepository(connection);
    assert.equal((await repo.list('robots')).meta.total, 3);
    assert.equal((await repo.list('components')).meta.total, 14);
    assert.equal((await repo.list('steps')).meta.total, 15);
    const fixture = require('./seed-fixture.json');
    for (const [kind, source] of [['robots', fixture.robots[0]], ['components', fixture.components[0]], ['steps', fixture.steps[0]], ['library-resources', fixture.library[0]]]) {
      const row = { ...source, id: `smoke-${kind}` };
      if (kind === 'steps') row.stepOrder = 9999;
      assert.equal((await repo.create(kind, row)).id, row.id);
      const patch = kind === 'robots' || kind === 'components' ? { name: 'Updated smoke' } : { title: 'Updated smoke' };
      assert.equal((await repo.update(kind, row.id, patch))[Object.keys(patch)[0]], 'Updated smoke');
      await repo.remove(kind, row.id);
      await assert.rejects(repo.get(kind, row.id), { status: 404 });
    }
    const duplicateStep = { ...fixture.steps[0], id: 'duplicate-order' };
    await assert.rejects(repo.create('steps', duplicateStep), { code: 'ER_DUP_ENTRY' });
    const filtered = await repo.list('steps', { robotId: 'mini-arm', page: 2, limit: 2 });
    assert.equal(filtered.meta.total, 5);
    assert.deepEqual(filtered.data.map(row => row.stepOrder), [3, 4]);
    const component = { ...(await repo.get('components', 'sg90')), id: 'test-content-servo' };
    assert.equal((await repo.create('components', component)).id, component.id);
    await assert.rejects(repo.create('components', component), { code: 'ER_DUP_ENTRY' });
    assert.equal((await repo.update('components', component.id, { name: 'Servo kiểm thử' })).name, 'Servo kiểm thử');
    await repo.setPart('mini-arm', { componentId: component.id, quantity: 2 });
    await assert.rejects(repo.remove('components', component.id), { code: 'ER_ROW_IS_REFERENCED_2' });
    await assert.rejects(repo.setPart('missing-robot', { componentId: component.id, quantity: 1 }), { code: 'ER_NO_REFERENCED_ROW_2' });
    await repo.removePart('mini-arm', component.id);
    await repo.remove('components', component.id);
    await assert.rejects(repo.get('components', component.id), { status: 404 });
    const [user] = await connection.execute('INSERT INTO users(email,display_name,password_hash) VALUES (?,?,?)', [`content-${Date.now()}@example.invalid`, 'Test', 'not-a-login-hash']);
    const [session] = await connection.execute('INSERT INTO assembly_sessions(user_id,robot_id) VALUES (?,?)', [user.insertId, 'mini-arm']);
    const [[created]] = await connection.execute('SELECT status FROM assembly_sessions WHERE id = ?', [session.insertId]);
    assert.equal(created.status, 'PREPARING');
    for (const state of ['READY', 'IN_PROGRESS', 'COMPLETED', 'ABANDONED']) {
      await connection.execute('UPDATE assembly_sessions SET status = ? WHERE id = ?', [state, session.insertId]);
    }
    await connection.execute('INSERT INTO auth_sessions(user_id,token_hash,expires_at) VALUES (?,?,DATE_ADD(UTC_TIMESTAMP(), INTERVAL 1 DAY))', [user.insertId, 'a'.repeat(64)]);
    await assert.rejects(connection.execute('INSERT INTO auth_sessions(user_id,token_hash,expires_at) VALUES (?,?,UTC_TIMESTAMP())', [user.insertId, 'a'.repeat(64)]), { code: 'ER_DUP_ENTRY' });
    await assert.rejects(connection.execute('INSERT INTO session_components(session_id,robot_id,component_id) VALUES (?,?,?)', [session.insertId, 'mini-arm', 'line-sensor']), { code: 'ER_NO_REFERENCED_ROW_2' });
    await connection.execute('INSERT INTO session_components(session_id,robot_id,component_id) VALUES (?,?,?)', [session.insertId, 'mini-arm', 'sg90']);
    await assert.rejects(repo.removePart('mini-arm', 'sg90'), { code: 'ER_ROW_IS_REFERENCED_2' });
    await assert.rejects(connection.execute('INSERT INTO session_steps(session_id,robot_id,step_id) VALUES (?,?,?)', [session.insertId, 'mini-arm', 'line-follower-step-1']), { code: 'ER_NO_REFERENCED_ROW_2' });
    await connection.execute('INSERT INTO session_steps(session_id,robot_id,step_id) VALUES (?,?,?)', [session.insertId, 'mini-arm', 'mini-arm-step-1']);
    await assert.rejects(repo.remove('steps', 'mini-arm-step-1'), { code: 'ER_ROW_IS_REFERENCED_2' });
    await connection.execute('DELETE FROM assembly_sessions WHERE id = ?', [session.insertId]);
    for (const table of ['session_components', 'session_steps']) {
      const [[count]] = await connection.execute(`SELECT COUNT(*) AS total FROM ${table} WHERE session_id = ?`, [session.insertId]);
      assert.equal(count.total, 0);
    }
    console.log('MySQL smoke passed; rolling back all test rows.');
  } finally { await connection.rollback(); await connection.end(); }
})().catch(error => { console.error('MySQL smoke failed:', error.code || error.message); process.exitCode = 1; });
