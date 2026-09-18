"use strict";

const assert = require("node:assert/strict");
const http = require("node:http");
const test = require("node:test");
const { createApp } = require("../../server/app");
const { loadConfig } = require("../../server/config/env");

function createRepositoryDoubles() {
  const usersByEmail = new Map();
  const usersById = new Map();
  const sessionsByHash = new Map();
  let nextUserId = 1;
  let nextSessionId = 1;

  const users = {
    async findByEmail(email) {
      return usersByEmail.get(email) || null;
    },
    async findPublicById(userId) {
      const user = usersById.get(userId);
      if (!user) return null;
      const { passwordHash, ...publicUser } = user;
      return publicUser;
    },
    async create(input) {
      const user = {
        id: String(nextUserId++),
        ...input,
        createdAt: new Date("2026-09-18T00:00:00.000Z")
      };
      usersByEmail.set(user.email, user);
      usersById.set(user.id, user);
      return user;
    }
  };

  const authSessions = {
    async create(input) {
      const session = {
        id: String(nextSessionId++),
        ...input,
        revokedAt: null
      };
      sessionsByHash.set(session.tokenHash, session);
      return session;
    },
    async findActiveByTokenHash(tokenHash) {
      const session = sessionsByHash.get(tokenHash);
      if (!session || session.revokedAt || session.expiresAt <= new Date()) return null;
      return session;
    },
    async revokeByTokenHash(tokenHash) {
      const session = sessionsByHash.get(tokenHash);
      if (session) session.revokedAt = new Date();
    },
    async revokeAllForUser() {}
  };

  return { users, authSessions, assemblySessions: {} };
}

async function withAuthServer(run) {
  const app = createApp({
    config: loadConfig({ NODE_ENV: "test", SESSION_TOKEN_TTL_MS: "3600000" }),
    database: null,
    repositories: createRepositoryDoubles(),
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

function sessionCookie(response) {
  return response.headers.get("set-cookie")?.split(";", 1)[0] || "";
}

test("register creates a public user and an HttpOnly session cookie", async () => {
  await withAuthServer(async (baseUrl) => {
    const response = await fetch(`${baseUrl}/api/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        fullName: "  Nguyễn Văn A  ",
        email: "  USER@Example.COM ",
        password: "Mat-khau-123"
      })
    });
    const body = await response.json();
    const setCookie = response.headers.get("set-cookie");

    assert.equal(response.status, 201);
    assert.equal(body.data.user.fullName, "Nguyễn Văn A");
    assert.equal(body.data.user.email, "user@example.com");
    assert.equal(body.data.user.role, "USER");
    assert.equal(body.data.user.passwordHash, undefined);
    assert.match(setCookie, /^ral_session=[^;]+;/);
    assert.match(setCookie, /HttpOnly/i);
    assert.match(setCookie, /SameSite=Lax/i);
    assert.match(setCookie, /Path=\/api/i);
  });
});

test("login, me and logout use the server-side session lifecycle", async () => {
  await withAuthServer(async (baseUrl) => {
    const credentials = { fullName: "Nguyễn Văn B", email: "member@example.com", password: "Mat-khau-456" };
    await fetch(`${baseUrl}/api/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(credentials)
    });

    const login = await fetch(`${baseUrl}/api/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: credentials.email, password: credentials.password })
    });
    const cookie = sessionCookie(login);
    assert.equal(login.status, 200);
    assert.ok(cookie.startsWith("ral_session="));

    const me = await fetch(`${baseUrl}/api/auth/me`, { headers: { Cookie: cookie } });
    const meBody = await me.json();
    assert.equal(me.status, 200);
    assert.equal(meBody.data.user.email, credentials.email);
    assert.match(meBody.csrfToken, /^[a-f0-9]{64}$/);

    const logout = await fetch(`${baseUrl}/api/auth/logout`, {
      method: "POST",
      headers: { Cookie: cookie }
    });
    assert.equal(logout.status, 204);
    assert.match(logout.headers.get("set-cookie"), /Max-Age=0/i);

    const afterLogout = await fetch(`${baseUrl}/api/auth/me`, { headers: { Cookie: cookie } });
    assert.equal(afterLogout.status, 401);
    assert.equal((await afterLogout.json()).error.code, "AUTH_REQUIRED");
  });
});

test("auth validation and invalid credentials return stable safe errors", async () => {
  await withAuthServer(async (baseUrl) => {
    const invalidRegistration = await fetch(`${baseUrl}/api/auth/register`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ fullName: "A", email: "not-an-email", password: "short" })
    });
    assert.equal(invalidRegistration.status, 422);
    assert.equal((await invalidRegistration.json()).error.code, "VALIDATION_ERROR");

    const invalidLogin = await fetch(`${baseUrl}/api/auth/login`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ email: "missing@example.com", password: "Mat-khau-789" })
    });
    const invalidBody = await invalidLogin.json();
    assert.equal(invalidLogin.status, 401);
    assert.equal(invalidBody.error.code, "INVALID_CREDENTIALS");
    assert.doesNotMatch(JSON.stringify(invalidBody), /missing@example\.com|passwordHash/i);
  });
});
