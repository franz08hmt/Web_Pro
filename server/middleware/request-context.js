"use strict";

const { randomUUID } = require("node:crypto");

function requestContext(logger) {
  return (request, response, next) => {
    const startedAt = performance.now();
    request.requestId = randomUUID();
    response.setHeader("X-Request-Id", request.requestId);
    response.on("finish", () => {
      logger.info({
        event: "request_completed",
        requestId: request.requestId,
        method: request.method,
        path: request.path,
        statusCode: response.statusCode,
        durationMs: Math.round(performance.now() - startedAt)
      });
    });
    next();
  };
}

module.exports = { requestContext };
