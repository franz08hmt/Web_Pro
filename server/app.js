"use strict";

const path = require("node:path");
const cors = require("cors");
const express = require("express");
const rateLimit = require("express-rate-limit");
const helmet = require("helmet");
const { loadConfig } = require("./config/env");
const { createLogger } = require("./config/logger");
const { ApiError } = require("./core/api-error");
const { createDatabasePool } = require("./database/pool");
const { errorHandler, notFound } = require("./middleware/error-handler");
const { requestContext } = require("./middleware/request-context");
const { createApiRouter } = require("./routes");

const PROJECT_ROOT = path.resolve(__dirname, "..");

function createApp(options = {}) {
  const config = options.config || loadConfig();
  const logger = options.logger || createLogger();
  const database = options.database === undefined ? createDatabasePool(config.database) : options.database;
  const app = express();

  app.disable("x-powered-by");
  app.locals.database = database;
  app.locals.config = config;
  app.locals.logger = logger;

  app.use(requestContext(logger));
  app.use(helmet());
  app.use(cors({
    credentials: true,
    origin(origin, callback) {
      if (!origin || config.corsOrigins.includes(origin)) return callback(null, true);
      return callback(new ApiError(403, "CORS_ORIGIN_DENIED", "Origin này không được phép truy cập API."));
    }
  }));
  app.use(express.json({ limit: "256kb" }));
  app.use("/api", rateLimit({
    windowMs: config.rateLimit.windowMs,
    limit: config.rateLimit.max,
    standardHeaders: true,
    legacyHeaders: false,
    handler(request, response, next) {
      next(new ApiError(429, "RATE_LIMITED", "Bạn đã gửi quá nhiều yêu cầu. Vui lòng thử lại sau."));
    }
  }));
  app.use("/api", createApiRouter({
    database,
    contentRepository: options.contentRepository,
    requireAdmin: options.requireAdmin,
    protectMutation: options.protectMutation
  }));
  app.get(["/", "/index.html"], (request, response) => {
    response.sendFile(path.join(PROJECT_ROOT, "index.html"));
  });
  app.use("/vendor/three", express.static(path.join(PROJECT_ROOT, "node_modules", "three", "build"), {
    dotfiles: "deny",
    index: false
  }));
  app.use("/assets", express.static(path.join(PROJECT_ROOT, "assets"), {
    dotfiles: "deny",
    index: false
  }));
  app.use("/pages", express.static(path.join(PROJECT_ROOT, "pages"), {
    dotfiles: "deny",
    index: false
  }));
  app.use(notFound);
  app.use(errorHandler(logger));

  return app;
}

module.exports = { createApp };
