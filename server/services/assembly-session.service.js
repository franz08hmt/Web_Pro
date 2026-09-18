"use strict";

const { ApiError } = require("../core/api-error");

function sessionNotFound() {
  return new ApiError(404, "NOT_FOUND", "Không tìm thấy phiên lắp ráp.");
}

function invalidTransition() {
  return new ApiError(409, "INVALID_STATE_TRANSITION", "Không thể chuyển sang trạng thái yêu cầu.");
}

function createAssemblySessionService({ repository, contentRepository }) {
  async function componentRelations(robotId) {
    return contentRepository.parts(robotId);
  }

  async function stepRecords(robotId) {
    const firstPage = await contentRepository.list("steps", { robotId, page: 1, limit: 100 });
    const steps = [...firstPage.data];
    const total = firstPage.meta?.total ?? steps.length;
    for (let page = 2; steps.length < total; page += 1) {
      const result = await contentRepository.list("steps", { robotId, page, limit: 100 });
      steps.push(...result.data);
      if (result.data.length === 0) break;
    }
    return steps;
  }

  function progressPercent(session, requiredCount) {
    if (requiredCount === 0) return 0;
    const prepared = new Set(
      session.components
        .filter((component) => component.isPrepared)
        .map((component) => component.componentId)
    ).size;
    return Math.floor((prepared / requiredCount) * 100);
  }

  function present(session, requiredCount) {
    return { ...session, progressPercent: progressPercent(session, requiredCount) };
  }

  async function synchronizePreparation(session) {
    const relations = await componentRelations(session.robotId);
    const percent = progressPercent(session, relations.length);
    if (!["PREPARING", "READY"].includes(session.status)) {
      return { session, requiredCount: relations.length };
    }

    const expectedStatus = percent === 100 && relations.length > 0 ? "READY" : "PREPARING";
    if (session.status === expectedStatus) {
      return { session, requiredCount: relations.length };
    }
    const updated = await repository.updateStatus({
      sessionId: session.id,
      userId: session.userId,
      status: expectedStatus
    });
    return { session: updated, requiredCount: relations.length };
  }

  async function owned(sessionId, userId) {
    const session = await repository.findOwnedById({ sessionId, userId });
    if (!session) throw sessionNotFound();
    return session;
  }

  async function show(session) {
    const synchronized = await synchronizePreparation(session);
    return present(synchronized.session, synchronized.requiredCount);
  }

  return {
    async createOrResume({ userId, robotId }) {
      try {
        await contentRepository.get("robots", robotId);
      } catch (error) {
        if (error.status === 404 || error.code === "NOT_FOUND") {
          throw new ApiError(422, "VALIDATION_ERROR", "Robot không tồn tại.");
        }
        throw error;
      }
      return show(await repository.createOrResume({ userId, robotId }));
    },

    async list({ userId, page, pageSize, status }) {
      const result = await repository.listByUser({ userId, page, pageSize, status });
      const items = [];
      for (const session of result.items) {
        const detail = await show(session);
        const { components, steps, ...summary } = detail;
        items.push(summary);
      }
      return { items, total: result.total };
    },

    async get({ sessionId, userId }) {
      return show(await owned(sessionId, userId));
    },

    async updateStatus({ sessionId, userId, status }) {
      const current = await owned(sessionId, userId);
      if (current.status === status) return show(current);

      const canAbandon = status === "ABANDONED" && current.status !== "COMPLETED";
      const canStart = current.status === "READY" && status === "IN_PROGRESS";
      const canComplete = current.status === "IN_PROGRESS" && status === "COMPLETED";
      if (!canAbandon && !canStart && !canComplete) throw invalidTransition();

      return show(await repository.updateStatus({ sessionId, userId, status }));
    },

    async setComponentPrepared({ sessionId, userId, componentId, isPrepared }) {
      const current = await owned(sessionId, userId);
      const relations = await componentRelations(current.robotId);
      if (!relations.some((item) => item.componentId === componentId)) {
        throw new ApiError(422, "VALIDATION_ERROR", "Linh kiện không thuộc robot của phiên.");
      }
      return show(await repository.upsertComponentProgress({
        sessionId,
        userId,
        componentId,
        isPrepared
      }));
    },

    async setStepStatus({ sessionId, userId, stepId, status }) {
      const current = await owned(sessionId, userId);
      const requiredSteps = await stepRecords(current.robotId);
      if (!requiredSteps.some((step) => step.id === stepId)) {
        throw new ApiError(422, "VALIDATION_ERROR", "Bước không thuộc robot của phiên.");
      }

      let updated = await repository.upsertStepProgress({ sessionId, userId, stepId, status });
      const completedIds = new Set(
        updated.steps.filter((step) => step.status === "COMPLETED").map((step) => step.stepId)
      );
      const allCompleted = requiredSteps.length > 0 && requiredSteps.every((step) => completedIds.has(step.id));
      if (updated.status === "IN_PROGRESS" && allCompleted) {
        updated = await repository.updateStatus({ sessionId, userId, status: "COMPLETED" });
      }
      return show(updated);
    }
  };
}

module.exports = { createAssemblySessionService };
