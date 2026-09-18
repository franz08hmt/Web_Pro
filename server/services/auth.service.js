"use strict";

const crypto = require("node:crypto");
const { ApiError } = require("../core/api-error");
const { DUMMY_PASSWORD_HASH, hashPassword, verifyPassword } = require("../security/password");

function validationError(message) {
  return new ApiError(422, "VALIDATION_ERROR", message);
}

function normalizeEmail(value) {
  if (typeof value !== "string") throw validationError("Email không hợp lệ.");
  const email = value.trim().toLowerCase();
  if (email.length > 254 || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    throw validationError("Email không hợp lệ.");
  }
  return email;
}

function validatePassword(value) {
  if (typeof value !== "string" || value.length < 8 || value.length > 72) {
    throw validationError("Mật khẩu phải dài từ 8 đến 72 ký tự.");
  }
  return value;
}

function validateFullName(value) {
  if (typeof value !== "string") throw validationError("Họ tên không hợp lệ.");
  const fullName = value.trim();
  if (fullName.length < 2 || fullName.length > 100) {
    throw validationError("Họ tên phải dài từ 2 đến 100 ký tự.");
  }
  return fullName;
}

function publicUser(user) {
  const { passwordHash, ...safeUser } = user;
  return safeUser;
}

function hashToken(token) {
  return crypto.createHash("sha256").update(token).digest("hex");
}

function csrfTokenFor(sessionToken) {
  return hashToken(`csrf:${sessionToken}`);
}

function createAuthService(options) {
  const { users, authSessions } = options.repositories;
  const sessionTtlMs = options.sessionTtlMs;
  const now = options.now || (() => new Date());
  const generateToken = options.generateToken || (() => crypto.randomBytes(32).toString("base64url"));
  const password = options.password || { hash: hashPassword, verify: verifyPassword };

  async function issueSession(userId) {
    const token = generateToken();
    const expiresAt = new Date(now().getTime() + sessionTtlMs);
    await authSessions.create({ userId, tokenHash: hashToken(token), expiresAt });
    return { token, expiresAt, csrfToken: csrfTokenFor(token) };
  }

  return {
    async register(input = {}) {
      const fullName = validateFullName(input.fullName);
      const email = normalizeEmail(input.email);
      const rawPassword = validatePassword(input.password);
      if (await users.findByEmail(email)) {
        throw new ApiError(409, "EMAIL_ALREADY_EXISTS", "Email đã được sử dụng.");
      }
      const passwordHash = await password.hash(rawPassword);
      const user = await users.create({ fullName, email, passwordHash, role: "USER" });
      return { user: publicUser(user), session: await issueSession(user.id) };
    },

    async login(input = {}) {
      const email = normalizeEmail(input.email);
      const rawPassword = validatePassword(input.password);
      const user = await users.findByEmail(email);
      const matches = await password.verify(rawPassword, user?.passwordHash || DUMMY_PASSWORD_HASH);
      if (!user || !matches) {
        throw new ApiError(401, "INVALID_CREDENTIALS", "Email hoặc mật khẩu không đúng.");
      }
      return { user: publicUser(user), session: await issueSession(user.id) };
    },

    async authenticate(token) {
      if (typeof token !== "string" || token.length < 20 || token.length > 200) return null;
      const session = await authSessions.findActiveByTokenHash(hashToken(token));
      if (!session) return null;
      const user = await users.findPublicById(session.userId);
      if (!user) return null;
      return { user, session, token, csrfToken: csrfTokenFor(token) };
    },

    async logout(token) {
      if (typeof token === "string" && token.length > 0) {
        await authSessions.revokeByTokenHash(hashToken(token));
      }
    }
  };
}

module.exports = { createAuthService };
