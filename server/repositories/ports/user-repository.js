"use strict";

const { NotImplementedError } = require("../../core/not-implemented-error");

/**
 * @typedef {Object} UserRecord
 * @property {string} id
 * @property {string} fullName
 * @property {string} email - Đã chuẩn hóa (trim + lowercase) trước khi lưu.
 * @property {string} passwordHash
 * @property {"USER"|"ADMIN"} role
 * @property {Date} createdAt
 */

/**
 * Hợp đồng mà mọi adapter (in-memory, MySQL...) phải cài đặt đầy đủ.
 * Service chỉ được phụ thuộc vào interface này, không bao giờ import
 * trực tiếp một adapter cụ thể.
 */
class UserRepositoryPort {
  /**
   * @param {string} email - Email đã chuẩn hóa (trim + lowercase).
   * @returns {Promise<UserRecord|null>}
   */
  async findByEmail(email) {
    throw new NotImplementedError("UserRepositoryPort", "findByEmail");
  }

  /**
   * @param {string} userId
   * @returns {Promise<Omit<UserRecord, "passwordHash">|null>}
   */
  async findPublicById(userId) {
    throw new NotImplementedError("UserRepositoryPort", "findPublicById");
  }

  /**
   * @param {{ fullName: string, email: string, passwordHash: string, role: "USER"|"ADMIN" }} input
   * @returns {Promise<UserRecord>}
   */
  async create(input) {
    throw new NotImplementedError("UserRepositoryPort", "create");
  }
}

module.exports = { UserRepositoryPort };
