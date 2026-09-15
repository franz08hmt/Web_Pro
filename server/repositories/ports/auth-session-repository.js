"use strict";

const { NotImplementedError } = require("../../core/not-implemented-error");

/**
 * @typedef {Object} AuthSessionRecord
 * @property {string} id
 * @property {string} userId
 * @property {string} tokenHash - SHA-256 hex của token gốc. Token gốc không bao giờ được lưu.
 * @property {Date} expiresAt
 * @property {Date|null} revokedAt
 */

class AuthSessionRepositoryPort {
  /**
   * @param {{ userId: string, tokenHash: string, expiresAt: Date }} input
   * @returns {Promise<AuthSessionRecord>}
   */
  async create(input) {
    throw new NotImplementedError("AuthSessionRepositoryPort", "create");
  }

  /**
   * Trả null nếu không tìm thấy, đã bị thu hồi, hoặc đã hết hạn.
   * @param {string} tokenHash
   * @returns {Promise<AuthSessionRecord|null>}
   */
  async findActiveByTokenHash(tokenHash) {
    throw new NotImplementedError("AuthSessionRepositoryPort", "findActiveByTokenHash");
  }

  /**
   * @param {string} tokenHash
   * @returns {Promise<void>}
   */
  async revokeByTokenHash(tokenHash) {
    throw new NotImplementedError("AuthSessionRepositoryPort", "revokeByTokenHash");
  }

  /**
   * Để sẵn cho tính năng "đăng xuất mọi thiết bị" — chưa có route nào gọi.
   * @param {string} userId
   * @returns {Promise<void>}
   */
  async revokeAllForUser(userId) {
    throw new NotImplementedError("AuthSessionRepositoryPort", "revokeAllForUser");
  }
}

module.exports = { AuthSessionRepositoryPort };
