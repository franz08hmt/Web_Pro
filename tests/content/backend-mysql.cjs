'use strict';
const assert = require('node:assert/strict');
const crypto = require('node:crypto');
const mysql = require('../../server/content/node_modules/mysql2/promise');
const { createBackendRepositories } = require('../../server/content/backend-repositories.cjs');

(async () => {
  if (!process.env.CONTENT_TEST_DB_NAME?.endsWith('_test')) throw Error('Separate _test database required.');
  const pool = mysql.createPool({ host: process.env.CONTENT_TEST_DB_HOST || '127.0.0.1', port: Number(process.env.CONTENT_TEST_DB_PORT || 3306), user: process.env.CONTENT_TEST_DB_USER, password: process.env.CONTENT_TEST_DB_PASSWORD, database: process.env.CONTENT_TEST_DB_NAME, connectionLimit: 4, timezone: 'Z', supportBigNumbers: true, bigNumberStrings: true });
  const { users, authSessions, assemblySessions } = createBackendRepositories(pool);
  const ids = [];
  try {
    const email = `adapter-${crypto.randomUUID()}@example.invalid`;
    const user = await users.create({ fullName: 'Adapter test', email, passwordHash: 'test-not-a-login-hash', role: 'ADMIN' });
    ids.push(user.id);
    const other = await users.create({ fullName: 'Other test', email: `other-${email}`, passwordHash: 'test-not-a-login-hash', role: 'USER' });
    ids.push(other.id);
    assert.equal(typeof user.id, 'string'); assert.equal(user.role, 'ADMIN');
    assert.equal(Object.hasOwn(await users.findPublicById(user.id), 'passwordHash'), false);
    assert.equal((await users.findByEmail(email.toUpperCase())).id, user.id);
    await assert.rejects(users.create({ fullName: 'Duplicate', email, passwordHash: 'x', role: 'USER' }), { status: 409, code: 'EMAIL_ALREADY_EXISTS' });
    const hash = crypto.randomBytes(32).toString('hex');
    const auth = await authSessions.create({ userId: user.id, tokenHash: hash, expiresAt: new Date(Date.now() + 60000) });
    assert.equal(typeof auth.id, 'string');
    assert.equal((await authSessions.findActiveByTokenHash(hash)).userId, user.id);
    await authSessions.revokeByTokenHash(hash);
    assert.equal(await authSessions.findActiveByTokenHash(hash), null);
    const expired = crypto.randomBytes(32).toString('hex');
    await authSessions.create({ userId: user.id, tokenHash: expired, expiresAt: new Date(Date.now() - 60000) });
    assert.equal(await authSessions.findActiveByTokenHash(expired), null);
    const active = crypto.randomBytes(32).toString('hex');
    await authSessions.create({ userId: user.id, tokenHash: active, expiresAt: new Date(Date.now() + 60000) });
    await authSessions.revokeAllForUser(user.id);
    assert.equal(await authSessions.findActiveByTokenHash(active), null);
    const requests = Array.from({ length: 4 }, () => assemblySessions.createOrResume({ userId: user.id, robotId: 'mini-arm' }));
    const sessions = await Promise.all(requests);
    assert.equal(new Set(sessions.map(row => row.id)).size, 1);
    const input = { sessionId: sessions[0].id, userId: user.id };
    assert.equal(sessions[0].status, 'PREPARING');
    assert.equal(await assemblySessions.findOwnedById({ ...input, userId: other.id }), null);
    await assert.rejects(assemblySessions.updateStatus({ ...input, userId: other.id, status: 'COMPLETED' }), { status: 404 });
    await assert.rejects(assemblySessions.upsertComponentProgress({ ...input, componentId: 'line-sensor', isPrepared: true }), { status: 422 });
    await assert.rejects(assemblySessions.upsertStepProgress({ ...input, stepId: 'line-follower-step-1', status: 'COMPLETED' }), { status: 422 });
    for (let i = 0; i < 2; i++) await assemblySessions.upsertComponentProgress({ ...input, componentId: 'sg90', isPrepared: true });
    await assemblySessions.upsertStepProgress({ ...input, stepId: 'mini-arm-step-1', status: 'COMPLETED' });
    await assemblySessions.setVisualPart({ ...input, componentId: 'sg90', isAssembled: true });
    let restored = await assemblySessions.findOwnedById(input);
    assert.equal(restored.components.length, 1); assert.equal(restored.components[0].isPrepared, true);
    assert.equal(restored.steps[0].status, 'COMPLETED'); assert.ok(restored.steps[0].updatedAt instanceof Date);
    assert.deepEqual(restored.assembledPartIds, ['sg90']);
    await assert.rejects(assemblySessions.setVisualPart({ ...input, userId: other.id, componentId: 'sg90', isAssembled: true }), { status: 404 });
    await assert.rejects(assemblySessions.setVisualPart({ ...input, componentId: 'line-sensor', isAssembled: true }), { status: 422 });
    await assemblySessions.setVisualPart({ ...input, componentId: 'sg90', isAssembled: false });
    restored = await assemblySessions.findOwnedById(input);
    assert.deepEqual(restored.assembledPartIds, []);
    restored = await assemblySessions.upsertStepProgress({ ...input, stepId: 'mini-arm-step-1', status: 'PENDING' });
    assert.equal(restored.steps[0].status, 'PENDING');
    await assemblySessions.updateStatus({ ...input, status: 'COMPLETED' });
    const next = await assemblySessions.createOrResume({ userId: user.id, robotId: 'mini-arm' });
    assert.notEqual(next.id, input.sessionId);
    assert.equal((await assemblySessions.listByUser({ userId: user.id, status: 'COMPLETED', page: 1, pageSize: 20 })).total, 1);
    assert.equal((await assemblySessions.listByUser({ userId: other.id, page: 1, pageSize: 20 })).total, 0);
    console.log('Backend MySQL adapters passed: user/auth/session ports, concurrency, ownership, progress, expiry and revocation.');
  } finally {
    // Adapter methods commit independently; remove only this run's generated users and sessions.
    try {
      for (const id of ids) {
        await pool.execute('DELETE FROM assembly_sessions WHERE user_id = ?', [id]);
        await pool.execute('DELETE FROM users WHERE id = ?', [id]);
      }
    } finally { await pool.end(); }
  }
})().catch(err => { console.error('Backend MySQL failed:', err.code || err.message); process.exitCode = 1; });
