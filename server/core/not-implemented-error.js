"use strict";

/**
 * Ném khi một repository port chưa có adapter thật cắm vào (ví dụ MySQL adapter
 * của Nhi chưa merge). Không phải lỗi nghiệp vụ — controller/service không nên
 * bắt lỗi này để trả về người dùng; nó chỉ nên xuất hiện khi cấu hình sai.
 */
class NotImplementedError extends Error {
  constructor(portName, methodName) {
    super(`${portName}.${methodName}() chưa có adapter thật. Xem docs/BACKEND_HANDOFF.md.`);
    this.name = "NotImplementedError";
    this.portName = portName;
    this.methodName = methodName;
  }
}

module.exports = { NotImplementedError };
