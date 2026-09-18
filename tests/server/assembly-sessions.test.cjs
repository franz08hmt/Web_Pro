"use strict";

const assert = require("node:assert/strict");
const crypto = require("node:crypto");
const http = require("node:http");
const test = require("node:test");
const { createApp } = require("../../server/app");
const { loadConfig } = require("../../server/config/env");

function createRepositories() {
  const users = new Map();
  const authSessions = new Map();
  const sessions = new Map();
  let nextUserId = 1;
  let nextSessionId = 1;

  return {
    users: {
      async findByEmail(email) { return [...users.values()].find((user) => user.email === email) || null; },
      async findPublicById(id) {
        const user = users.get(id);
        if (!user) return null;
        const { passwordHash, ...result } = user;
        return result;
      },
      async create(input) {
        const user = { id: String(nextUserId++), ...input, createdAt: new Date() };
        users.set(user.id, user);
        return user;
      }
    },
    authSessions: {
      async create(input) {
        const record = { id: crypto.randomUUID(), ...input, revokedAt: null };
        authSessions.set(input.tokenHash, record);
        return record;
      },
      async findActiveByTokenHash(hash) { return authSessions.get(hash) || null; },
      async revokeByTokenHash(hash) { authSessions.delete(hash); },
      async revokeAllForUser() {}
    },
    assemblySessions: {
      async createOrResume({ userId, robotId }) {
        let session = [...sessions.values()].find((item) =>
          item.userId === userId && item.robotId === robotId &&
          ["PREPARING", "READY", "IN_PROGRESS"].includes(item.status)
        );
        if (!session) {
          session = {
            id: String(nextSessionId++),
            userId,
            robotId,
            status: "PREPARING",
            components: [],
            steps: [],
            createdAt: new Date(),
            updatedAt: new Date()
          };
          sessions.set(session.id, session);
        }
        return structuredClone(session);
      },
      async listByUser({ userId, status }) {
        const items = [...sessions.values()].filter((item) =>
          item.userId === userId && (!status || item.status === status)
        );
        return { items: structuredClone(items), total: items.length };
      },
      async findOwnedById({ sessionId, userId }) {
        const session = sessions.get(sessionId);
        return session?.userId === userId ? structuredClone(session) : null;
      },
      async updateStatus({ sessionId, userId, status }) {
        const session = sessions.get(sessionId);
        assert.equal(session.userId, userId);
        session.status = status;
        session.updatedAt = new Date();
        return structuredClone(session);
      },
      async upsertComponentProgress({ sessionId, userId, componentId, isPrepared }) {
        const session = sessions.get(sessionId);
        assert.equal(session.userId, userId);
        const current = session.components.find((item) => item.componentId === componentId);
        if (current) current.isPrepared = isPrepared;
        else session.components.push({ componentId, isPrepared, updatedAt: new Date() });
        return structuredClone(session);
      },
      async upsertStepProgress({ sessionId, userId, stepId, status }) {
        const session = sessions.get(sessionId);
        assert.equal(session.userId, userId);
        const current = session.steps.find((item) => item.stepId === stepId);
        if (current) current.status = status;
        else session.steps.push({ stepId, status, updatedAt: new Date() });
        return structuredClone(session);
      }
    }
  };
}

function createContentRepository() {
  return {
    async get(kind, id) {
      if (kind === "robots" && id === "line-follower") return { id, name: "Robot dò đường" };
      throw Object.assign(new Error("Không tìm thấy robot."), { status: 404, code: "NOT_FOUND" });
    },
    async parts(robotId) {
      assert.equal(robotId, "line-follower");
      return [
        { componentId: "arduino-uno", quantity: 1 },
        { componentId: "line-sensor", quantity: 2 }
      ];
    },
    async list(kind, options) {
      if (kind === "steps" && options.robotId === "line-follower") {
        return {
          data: [
            { id: "line-follower-step-1" },
            { id: "line-follower-step-2" }
          ],
          meta: { page: 1, limit: 100, total: 2 }
        };
      }
      return { data: [], meta: { page: 1, limit: 20, total: 0 } };
    },
    async create() {},
    async update() {},
    async remove() {},
    async setPart() {},
    async removePart() {}
  };
}

async function withServer(run) {
  const app = createApp({
    config: loadConfig({ NODE_ENV: "test" }),
    database: null,
    repositories: createRepositories(),
    contentRepository: createContentRepository(),
    logger: { info() {}, error() {} }
  });
  const server = http.createServer(app);
  await new Promise((resolve) => server.listen(0, "127.0.0.1", resolve));
  const { port } = server.address();
  try {
    await run(`http://127.0.0.1:${port}`);
  } finally {
    await new Promise((resolve, reject) => server.close((error) => error ? reject(error) : resolve()));
  }
}

async function createAuthenticatedClient(baseUrl, email = "builder@example.com") {
  const registration = await fetch(`${baseUrl}/api/auth/register`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ fullName: "Người lắp ráp", email, password: "Mat-khau-123" })
  });
  const cookie = registration.headers.get("set-cookie").split(";", 1)[0];
  const me = await fetch(`${baseUrl}/api/auth/me`, { headers: { Cookie: cookie } });
  const csrfToken = (await me.json()).csrfToken;
  const headers = { Cookie: cookie, "Content-Type": "application/json", "X-CSRF-Token": csrfToken };
  return { cookie, headers };
}

test("assembly session API persists preparation and completes the documented state machine", async () => {
  await withServer(async (baseUrl) => {
    const client = await createAuthenticatedClient(baseUrl);

    const unknownRobot = await fetch(`${baseUrl}/api/assembly-sessions`, {
      method: "POST",
      headers: client.headers,
      body: JSON.stringify({ robotId: "unknown-robot" })
    });
    assert.equal(unknownRobot.status, 422);
    assert.equal((await unknownRobot.json()).error.code, "VALIDATION_ERROR");

    const missingCsrf = await fetch(`${baseUrl}/api/assembly-sessions`, {
      method: "POST",
      headers: { Cookie: client.cookie, "Content-Type": "application/json" },
      body: JSON.stringify({ robotId: "line-follower" })
    });
    assert.equal(missingCsrf.status, 403);

    const create = await fetch(`${baseUrl}/api/assembly-sessions`, {
      method: "POST",
      headers: client.headers,
      body: JSON.stringify({ robotId: "line-follower" })
    });
    const created = (await create.json()).data;
    assert.equal(create.status, 201);
    assert.equal(created.status, "PREPARING");
    assert.equal(created.progressPercent, 0);

    const anotherUser = await createAuthenticatedClient(baseUrl, "other@example.com");
    const forbiddenSession = await fetch(`${baseUrl}/api/assembly-sessions/${created.id}`, {
      headers: { Cookie: anotherUser.cookie }
    });
    assert.equal(forbiddenSession.status, 404);
    assert.equal((await forbiddenSession.json()).error.code, "NOT_FOUND");

    const firstComponent = await fetch(`${baseUrl}/api/assembly-sessions/${created.id}/components/arduino-uno`, {
      method: "PUT",
      headers: client.headers,
      body: JSON.stringify({ isPrepared: true })
    });
    const halfway = (await firstComponent.json()).data;
    assert.equal(halfway.status, "PREPARING");
    assert.equal(halfway.progressPercent, 50);

    const secondComponent = await fetch(`${baseUrl}/api/assembly-sessions/${created.id}/components/line-sensor`, {
      method: "PUT",
      headers: client.headers,
      body: JSON.stringify({ isPrepared: true })
    });
    const ready = (await secondComponent.json()).data;
    assert.equal(ready.status, "READY");
    assert.equal(ready.progressPercent, 100);

    const start = await fetch(`${baseUrl}/api/assembly-sessions/${created.id}`, {
      method: "PATCH",
      headers: client.headers,
      body: JSON.stringify({ status: "IN_PROGRESS" })
    });
    assert.equal((await start.json()).data.status, "IN_PROGRESS");

    await fetch(`${baseUrl}/api/assembly-sessions/${created.id}/steps/line-follower-step-1`, {
      method: "PUT",
      headers: client.headers,
      body: JSON.stringify({ status: "COMPLETED" })
    });
    const finalStep = await fetch(`${baseUrl}/api/assembly-sessions/${created.id}/steps/line-follower-step-2`, {
      method: "PUT",
      headers: client.headers,
      body: JSON.stringify({ status: "COMPLETED" })
    });
    const completed = (await finalStep.json()).data;
    assert.equal(completed.status, "COMPLETED");
    assert.equal(completed.progressPercent, 100);

    const invalidTransition = await fetch(`${baseUrl}/api/assembly-sessions/${created.id}`, {
      method: "PATCH",
      headers: client.headers,
      body: JSON.stringify({ status: "IN_PROGRESS" })
    });
    assert.equal(invalidTransition.status, 409);
    assert.equal((await invalidTransition.json()).error.code, "INVALID_STATE_TRANSITION");

    const list = await fetch(`${baseUrl}/api/assembly-sessions?page=1&pageSize=20`, {
      headers: { Cookie: client.cookie }
    });
    const listBody = await list.json();
    assert.equal(list.status, 200);
    assert.equal(listBody.data.length, 1);
    assert.deepEqual(listBody.meta, { page: 1, pageSize: 20, total: 1 });
    assert.equal(listBody.data[0].components, undefined);
    assert.equal(listBody.data[0].steps, undefined);
  });
});
