"use strict";

const { Router } = require("express");
const { ApiError } = require("../core/api-error");
const { createAuthMiddleware } = require("../middleware/auth");
const { createAuthService } = require("../services/auth.service");
const { createAuthRouter } = require("./auth.routes");
const { healthRouter } = require("./health.routes");
const { createContentRouter } = require("../content/index.cjs");

const CONTENT_PATH = /^\/(?:admin\/)?(?:robots|components|steps|library-resources)(?:\/|$)/;

function authenticationRequired(request, response, next) {
  next(new ApiError(
    401,
    "AUTH_REQUIRED",
    "Cần đăng nhập bằng tài khoản quản trị."
  ));
}

function csrfRequired(request, response, next) {
  next(new ApiError(
    403,
    "CSRF_REQUIRED",
    "Yêu cầu quản trị cần mã CSRF hợp lệ."
  ));
}

function createApiRouter(options = {}) {
  const router = Router();
  router.use(healthRouter);

  let authMiddleware = null;
  if (options.repositories?.users && options.repositories?.authSessions) {
    const authService = createAuthService({
      repositories: options.repositories,
      sessionTtlMs: options.config.auth.sessionTokenTtlMs
    });
    authMiddleware = createAuthMiddleware(authService);
    router.use(authMiddleware.attach);
    router.use("/auth", createAuthRouter({
      authService,
      middleware: authMiddleware,
      isProduction: options.config.isProduction
    }));
  } else {
    router.use("/auth", (request, response, next) => next(new ApiError(
      503,
      "DEPENDENCY_NOT_READY",
      "Dịch vụ xác thực chưa được cấu hình."
    )));
  }

  if (options.database || options.contentRepository) {
    router.use(createContentRouter({
      Router,
      pool: options.database,
      repository: options.contentRepository,
      requireAdmin: options.requireAdmin || authMiddleware?.requireAdmin || authenticationRequired,
      protectMutation: options.protectMutation || authMiddleware?.protectMutation || csrfRequired
    }));
  } else {
    router.use((request, response, next) => {
      if (!CONTENT_PATH.test(request.path)) return next();
      return next(new ApiError(
        503,
        "DATABASE_NOT_CONFIGURED",
        "Cơ sở dữ liệu nội dung chưa được cấu hình."
      ));
    });
  }

  return router;
}

module.exports = { createApiRouter };
