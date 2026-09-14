"use strict";

const { ApiError } = require("../core/api-error");
const { sendError } = require("../utils/api-response");

function notFound(request, response) {
  return sendError(response, {
    status: 404,
    code: "NOT_FOUND",
    message: "Không tìm thấy tài nguyên yêu cầu.",
    requestId: request.requestId
  });
}

function errorHandler(logger) {
  return (error, request, response, next) => {
    if (response.headersSent) return next(error);

    const invalidJson = error instanceof SyntaxError && Object.hasOwn(error, "body");
    const knownError = error instanceof ApiError;
    const status = knownError ? error.status : invalidJson ? 400 : 500;
    const code = knownError ? error.code : invalidJson ? "INVALID_JSON" : "INTERNAL_SERVER_ERROR";
    const message = knownError ? error.message : invalidJson
      ? "Nội dung JSON không hợp lệ."
      : "Đã xảy ra lỗi máy chủ. Vui lòng thử lại sau.";

    logger.error({
      event: "request_failed",
      requestId: request.requestId,
      method: request.method,
      path: request.path,
      statusCode: status,
      code,
      message: error.message
    });
    return sendError(response, { status, code, message, details: knownError ? error.details : undefined, requestId: request.requestId });
  };
}

module.exports = { errorHandler, notFound };
