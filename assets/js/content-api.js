"use strict";

(() => {
  const STATIC_PREVIEW_PORTS = new Set(["4173", "63342"]);
  const configuredBase = document.querySelector('meta[name="robot-api-base"]')?.content?.trim();
  const apiBase = configuredBase || `${window.location.origin}/api`;
  const enabled = Boolean(configuredBase) || !STATIC_PREVIEW_PORTS.has(window.location.port);

  function assertList(payload) {
    if (!payload || !Array.isArray(payload.data) || !payload.meta) {
      throw new Error("Dữ liệu API không hợp lệ.");
    }
    return payload.data;
  }

  async function request(path) {
    const controller = new AbortController();
    const timeout = setTimeout(() => controller.abort(), 8000);

    try {
      const response = await fetch(new URL(`${apiBase}${path}`, window.location.origin), {
        credentials: "same-origin",
        headers: { Accept: "application/json" },
        signal: controller.signal
      });
      const payload = await response.json().catch(() => null);

      if (!response.ok) {
        throw new Error(payload?.error?.message || `API nội dung phản hồi ${response.status}.`);
      }
      return payload;
    } catch (error) {
      if (error.name === "AbortError") {
        throw new Error("API nội dung phản hồi quá chậm.");
      }
      throw error;
    } finally {
      clearTimeout(timeout);
    }
  }

  async function list(kind) {
    if (!enabled) return null;
    return assertList(await request(`/${kind}?limit=100`));
  }

  let componentsPromise;
  let libraryPromise;
  let robotsPromise;

  function loadComponents() {
    componentsPromise ||= list("components");
    return componentsPromise;
  }

  function loadLibraryResources() {
    libraryPromise ||= list("library-resources");
    return libraryPromise;
  }

  function loadRobots() {
    if (!enabled) return null;

    robotsPromise ||= (async () => {
      const [robots, components] = await Promise.all([
        list("robots"),
        loadComponents()
      ]);
      const componentNames = new Map(components.map(component => [component.id, component.name]));

      return Promise.all(robots.map(async robot => {
        const [relationsPayload, stepsPayload] = await Promise.all([
          request(`/robots/${encodeURIComponent(robot.id)}/components`),
          request(`/robots/${encodeURIComponent(robot.id)}/steps?limit=100`)
        ]);
        if (!Array.isArray(relationsPayload?.data)) {
          throw new Error("Dữ liệu API không hợp lệ.");
        }
        const stepRecords = assertList(stepsPayload);

        return {
          ...robot,
          parts: relationsPayload.data.map(part => ({
            id: part.componentId,
            name: componentNames.get(part.componentId) || part.componentId,
            quantity: part.quantity
          })),
          steps: stepRecords.map(step => step.instruction),
          stepRecords
        };
      }));
    })();

    return robotsPromise;
  }

  window.RobotContentApi = Object.freeze({
    enabled,
    loadComponents,
    loadLibraryResources,
    loadRobots
  });
})();
