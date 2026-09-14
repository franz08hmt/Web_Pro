"use strict";

function sendSuccess(response, data, status = 200, meta) {
  const body = { data };
  if (meta !== undefined) body.meta = meta;
  return response.status(status).json(body);
}

function sendError(response, { status, code, message, details, requestId }) {
  const error = { code, message };
  if (details !== undefined) error.details = details;
  if (requestId) error.requestId = requestId;
  return response.status(status).json({ error });
}

module.exports = { sendError, sendSuccess };
