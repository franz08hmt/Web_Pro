'use strict';
const { test } = require('node:test');
const assert = require('node:assert/strict');
const { createBackendRepositories } = require('../../server/content/backend-repositories.cjs');
const { UserRepositoryPort } = require('../../server/repositories/ports/user-repository');
const { AuthSessionRepositoryPort } = require('../../server/repositories/ports/auth-session-repository');
const { AssemblySessionRepositoryPort } = require('../../server/repositories/ports/assembly-session-repository');

test('MySQL adapters implement every published backend port method', () => {
  const adapters = createBackendRepositories({});
  for (const [name, Port] of [['users', UserRepositoryPort], ['authSessions', AuthSessionRepositoryPort], ['assemblySessions', AssemblySessionRepositoryPort]]) {
    for (const method of Object.getOwnPropertyNames(Port.prototype).filter(key => key !== 'constructor')) assert.equal(typeof adapters[name][method], 'function', `${name}.${method}`);
  }
});
test('database IDs reject MySQL numeric coercion before issuing SQL', async () => {
  const adapters = createBackendRepositories({ execute() { assert.fail('Invalid input must not reach MySQL'); } });
  for (const id of ['1suffix', '1e0', '0', '-1', '18446744073709551616', 1]) {
    assert.throws(() => adapters.users.findPublicById(id), { status: 422 });
    await assert.rejects(adapters.assemblySessions.createOrResume({ userId: id, robotId: 'mini-arm' }), { status: 422 });
  }
});
