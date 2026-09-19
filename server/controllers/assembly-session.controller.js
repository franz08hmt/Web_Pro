"use strict";

const { ApiError } = require("../core/api-error");

const SESSION_STATUSES = new Set(["PREPARING", "READY", "IN_PROGRESS", "COMPLETED", "ABANDONED"]);

function validationError(message) {
  return new ApiError(422, "VALIDATION_ERROR", message);
}

function objectBody(body, allowedKeys) {
  if (!body || typeof body !== "object" || Array.isArray(body)) throw validationError("Body phải là object JSON.");
  const keys = Object.keys(body);
  if (keys.length !== allowedKeys.length || keys.some((key) => !allowedKeys.includes(key))) {
    throw validationError(`Body chỉ chứa ${allowedKeys.join(", ")}.`);
  }
  return body;
}

function slug(value, label) {
  if (typeof value !== "string" || !/^[a-z0-9]+(?:-[a-z0-9]+)*$/.test(value) || value.length > 64) {
    throw validationError(`${label} phải là slug hợp lệ.`);
  }
  return value;
}

function sessionId(value) {
  if (typeof value !== "string" || !/^[1-9][0-9]{0,19}$/.test(value)) {
    throw validationError("sessionId không hợp lệ.");
  }
  return value;
}

function positiveInteger(value, fallback, maximum) {
  if (value === undefined) return fallback;
  if (typeof value !== "string" || !/^[1-9][0-9]*$/.test(value)) throw validationError("Phân trang không hợp lệ.");
  const number = Number(value);
  if (!Number.isSafeInteger(number) || number > maximum) throw validationError("Phân trang không hợp lệ.");
  return number;
}

function createAssemblySessionController(service) {
  return {
    async create(request, response, next) {
      try {
        const body = objectBody(request.body, ["robotId"]);
        const data = await service.createOrResume({ userId: request.user.id, robotId: slug(body.robotId, "robotId") });
        response.status(201).json({ data });
      } catch (error) { next(error); }
    },

    async list(request, response, next) {
      try {
        const page = positiveInteger(request.query.page, 1, 100000);
        const pageSize = positiveInteger(request.query.pageSize, 20, 100);
        const status = request.query.status;
        if (status !== undefined && !SESSION_STATUSES.has(status)) throw validationError("Trạng thái không hợp lệ.");
        const result = await service.list({ userId: request.user.id, page, pageSize, status });
        response.json({ data: result.items, meta: { page, pageSize, total: result.total } });
      } catch (error) { next(error); }
    },

    async get(request, response, next) {
      try {
        const data = await service.get({ sessionId: sessionId(request.params.sessionId), userId: request.user.id });
        response.json({ data });
      } catch (error) { next(error); }
    },

    async updateStatus(request, response, next) {
      try {
        const body = objectBody(request.body, ["status"]);
        if (!SESSION_STATUSES.has(body.status)) throw validationError("Trạng thái không hợp lệ.");
        const data = await service.updateStatus({
          sessionId: sessionId(request.params.sessionId),
          userId: request.user.id,
          status: body.status
        });
        response.json({ data });
      } catch (error) { next(error); }
    },

    async setComponent(request, response, next) {
      try {
        const body = objectBody(request.body, ["isPrepared"]);
        if (typeof body.isPrepared !== "boolean") throw validationError("isPrepared phải là boolean.");
        const data = await service.setComponentPrepared({
          sessionId: sessionId(request.params.sessionId),
          userId: request.user.id,
          componentId: slug(request.params.componentId, "componentId"),
          isPrepared: body.isPrepared
        });
        response.json({ data });
      } catch (error) { next(error); }
    },

    async setStep(request, response, next) {
      try {
        const body = objectBody(request.body, ["status"]);
        if (!["PENDING", "COMPLETED"].includes(body.status)) throw validationError("Trạng thái bước không hợp lệ.");
        const data = await service.setStepStatus({
          sessionId: sessionId(request.params.sessionId),
          userId: request.user.id,
          stepId: slug(request.params.stepId, "stepId"),
          status: body.status
        });
        response.json({ data });
      } catch (error) { next(error); }
    },

    async setVisualPart(request, response, next) {
      try {
        const body = objectBody(request.body, ["isAssembled"]);
        if (typeof body.isAssembled !== "boolean") throw validationError("isAssembled phải là boolean.");
        const data = await service.setVisualPart({
          sessionId: sessionId(request.params.sessionId),
          userId: request.user.id,
          componentId: slug(request.params.componentId, "componentId"),
          isAssembled: body.isAssembled
        });
        response.json({ data });
      } catch (error) { next(error); }
    }
  };
}

module.exports = { createAssemblySessionController };
