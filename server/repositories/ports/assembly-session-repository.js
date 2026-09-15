"use strict";

const { NotImplementedError } = require("../../core/not-implemented-error");

/**
 * @typedef {"PREPARING"|"READY"|"IN_PROGRESS"|"COMPLETED"|"ABANDONED"} SessionStatus
 * @typedef {"PENDING"|"COMPLETED"} StepStatus
 *
 * @typedef {Object} ComponentProgressRecord
 * @property {string} componentId
 * @property {boolean} isPrepared
 * @property {Date} updatedAt
 *
 * @typedef {Object} StepProgressRecord
 * @property {string} stepId
 * @property {StepStatus} status
 * @property {Date} updatedAt
 *
 * @typedef {Object} AssemblySessionRecord
 * @property {string} id
 * @property {string} userId
 * @property {string} robotId
 * @property {SessionStatus} status
 * @property {ComponentProgressRecord[]} components
 * @property {StepProgressRecord[]} steps
 * @property {Date} createdAt
 * @property {Date} updatedAt
 */

class AssemblySessionRepositoryPort {
  /**
   * Tạo phiên mới, hoặc trả phiên PREPARING/READY/IN_PROGRESS gần nhất của
   * user cho đúng robotId nếu đã tồn tại (không tạo phiên trùng khi đang dở).
   * @param {{ userId: string, robotId: string }} input
   * @returns {Promise<AssemblySessionRecord>}
   */
  async createOrResume(input) {
    throw new NotImplementedError("AssemblySessionRepositoryPort", "createOrResume");
  }

  /**
   * @param {{ userId: string, page: number, pageSize: number, status?: SessionStatus }} input
   * @returns {Promise<{ items: AssemblySessionRecord[], total: number }>}
   */
  async listByUser(input) {
    throw new NotImplementedError("AssemblySessionRepositoryPort", "listByUser");
  }

  /**
   * Trả null nếu không tồn tại HOẶC không thuộc userId này — service dùng kết
   * quả null để trả 403/404 mà không phân biệt hai trường hợp cho client.
   * @param {{ sessionId: string, userId: string }} input
   * @returns {Promise<AssemblySessionRecord|null>}
   */
  async findOwnedById(input) {
    throw new NotImplementedError("AssemblySessionRepositoryPort", "findOwnedById");
  }

  /**
   * @param {{ sessionId: string, userId: string, status: SessionStatus }} input
   * @returns {Promise<AssemblySessionRecord>}
   */
  async updateStatus(input) {
    throw new NotImplementedError("AssemblySessionRepositoryPort", "updateStatus");
  }

  /**
   * Idempotent: gọi lại với cùng isPrepared không tạo bản ghi mới.
   * @param {{ sessionId: string, userId: string, componentId: string, isPrepared: boolean }} input
   * @returns {Promise<AssemblySessionRecord>}
   */
  async upsertComponentProgress(input) {
    throw new NotImplementedError("AssemblySessionRepositoryPort", "upsertComponentProgress");
  }

  /**
   * Idempotent: gọi lại với cùng status không tạo bản ghi mới.
   * @param {{ sessionId: string, userId: string, stepId: string, status: StepStatus }} input
   * @returns {Promise<AssemblySessionRecord>}
   */
  async upsertStepProgress(input) {
    throw new NotImplementedError("AssemblySessionRepositoryPort", "upsertStepProgress");
  }
}

module.exports = { AssemblySessionRepositoryPort };
