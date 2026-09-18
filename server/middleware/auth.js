"use strict";

const crypto = require("node:crypto");
const { ApiError } = require("../core/api-error");

const SESSION_COOKIE = "ral_session";

function readCookie(header, name) {
  if (typeof header !== "string") return null;
  for (const item of header.split(";")) {
    const separator = item.indexOf("=");
    if (separator < 0) continue;
    const key = item.slice(0, separator).trim();
    if (key !== name) continue;
    try {
      return decodeURIComponent(item.slice(separator + 1).trim());
    } catch {
      return null;
    }
  }
  return null;
}

function safeEqual(left, right) {
  if (typeof left !== "string" || typeof right !== "string") return false;
  const leftBuffer = Buffer.from(left);
  const rightBuffer = Buffer.from(right);
  return leftBuffer.length === rightBuffer.length && crypto.timingSafeEqual(leftBuffer, rightBuffer);
}

function createAuthMiddleware(authService) {
  async function attach(request, response, next) {
    try {
      const token = readCookie(request.headers.cookie, SESSION_COOKIE);
      request.auth = token ? await authService.authenticate(token) : null;
      request.user = request.auth?.user || null;
      next();
    } catch (error) {
      next(error);
    }
  }

  function requireAuth(request, response, next) {
    if (!request.user) {
      return next(new ApiError(401, "AUTH_REQUIRED", "Bạn cần đăng nhập để tiếp tục."));
    }
    return next();
  }

  function requireAdmin(request, response, next) {
    if (!request.user) {
      return next(new ApiError(401, "AUTH_REQUIRED", "Cần đăng nhập bằng tài khoản quản trị."));
    }
    if (request.user.role !== "ADMIN") {
      return next(new ApiError(403, "FORBIDDEN", "Tài khoản không có quyền quản trị nội dung."));
    }
    return next();
  }

  function protectMutation(request, response, next) {
    if (!request.auth) {
      return next(new ApiError(401, "AUTH_REQUIRED", "Bạn cần đăng nhập để tiếp tục."));
    }
    if (!safeEqual(request.get("X-CSRF-Token"), request.auth.csrfToken)) {
      return next(new ApiError(403, "CSRF_REQUIRED", "Yêu cầu cần mã CSRF hợp lệ."));
    }
    return next();
  }

  return { attach, protectMutation, requireAdmin, requireAuth };
}

module.exports = { SESSION_COOKIE, createAuthMiddleware, readCookie };
