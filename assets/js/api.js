"use strict";

(() => {
  const configuredBase = document.querySelector('meta[name="robot-api-base"]')?.content?.trim();
  const apiBase = configuredBase || `${window.location.origin}/api`;
  let csrfToken = null;

  function clientError(code, message, status) {
    const error = new Error(message);
    error.code = code;
    error.status = status;
    return error;
  }

  async function request(path, options = {}) {
    const method = options.method || "GET";
    if (options.csrf && !csrfToken) await refreshSession();

    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 8000);
    const headers = { Accept: "application/json" };
    if (options.body !== undefined) headers["Content-Type"] = "application/json";
    if (options.csrf) headers["X-CSRF-Token"] = csrfToken;

    try {
      const response = await fetch(new URL(`${apiBase}${path}`, window.location.origin), {
        method,
        credentials: "same-origin",
        headers,
        body: options.body === undefined ? undefined : JSON.stringify(options.body),
        signal: controller.signal
      });
      const payload = response.status === 204 ? null : await response.json().catch(() => null);
      if (!response.ok) {
        throw clientError(
          payload?.error?.code || "REQUEST_FAILED",
          payload?.error?.message || "Không thể hoàn tất yêu cầu.",
          response.status
        );
      }
      return payload;
    } catch (error) {
      if (error?.code && Number.isInteger(error.status)) throw error;
      if (error?.name === "AbortError") {
        throw clientError("NETWORK_ERROR", "Máy chủ phản hồi quá chậm. Vui lòng thử lại.", 0);
      }
      throw clientError("NETWORK_ERROR", "Không thể kết nối máy chủ. Vui lòng kiểm tra kết nối và thử lại.", 0);
    } finally {
      clearTimeout(timeout);
    }
  }

  async function refreshSession() {
    const payload = await request("/auth/me");
    csrfToken = payload?.csrfToken || null;
    return payload.data.user;
  }

  async function authenticate(path, input) {
    const payload = await request(path, { method: "POST", body: input });
    await refreshSession();
    return payload.data.user;
  }

  function segment(value) {
    return encodeURIComponent(String(value));
  }

  const auth = Object.freeze({
    register(input) { return authenticate("/auth/register", input); },
    login(input) { return authenticate("/auth/login", input); },
    me() { return refreshSession(); },
    async logout() {
      try {
        await request("/auth/logout", { method: "POST" });
      } finally {
        csrfToken = null;
      }
    }
  });

  const assemblySessions = Object.freeze({
    async createOrResume(robotId) {
      return (await request("/assembly-sessions", {
        method: "POST",
        csrf: true,
        body: { robotId }
      })).data;
    },
    async list(params = {}) {
      const query = new URLSearchParams();
      if (params.status) query.set("status", params.status);
      if (params.page !== undefined) query.set("page", params.page);
      if (params.pageSize !== undefined) query.set("pageSize", params.pageSize);
      const suffix = query.size ? `?${query}` : "";
      const payload = await request(`/assembly-sessions${suffix}`);
      return { items: payload.data, meta: payload.meta };
    },
    async get(sessionId) {
      return (await request(`/assembly-sessions/${segment(sessionId)}`)).data;
    },
    async updateStatus(sessionId, status) {
      return (await request(`/assembly-sessions/${segment(sessionId)}`, {
        method: "PATCH",
        csrf: true,
        body: { status }
      })).data;
    },
    async setComponentPrepared(sessionId, componentId, isPrepared) {
      return (await request(`/assembly-sessions/${segment(sessionId)}/components/${segment(componentId)}`, {
        method: "PUT",
        csrf: true,
        body: { isPrepared }
      })).data;
    },
    async setStepStatus(sessionId, stepId, status) {
      return (await request(`/assembly-sessions/${segment(sessionId)}/steps/${segment(stepId)}`, {
        method: "PUT",
        csrf: true,
        body: { status }
      })).data;
    },
    async setVisualPart(sessionId, componentId, isAssembled) {
      return (await request(`/assembly-sessions/${segment(sessionId)}/visual-parts/${segment(componentId)}`, {
        method: "PUT",
        csrf: true,
        body: { isAssembled }
      })).data;
    }
  });

  window.RobotAssemblyApi = Object.freeze({ auth, assemblySessions });
})();
