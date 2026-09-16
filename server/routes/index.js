"use strict";

const { Router } = require("express");
const { ApiError } = require("../core/api-error");
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

  if (options.database || options.contentRepository) {
    router.use(createContentRouter({
      Router,
      pool: options.database,
      repository: options.contentRepository,
      requireAdmin: options.requireAdmin || authenticationRequired,
      protectMutation: options.protectMutation || csrfRequired
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
