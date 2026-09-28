"use strict";

(() => {
  const api = window.RobotAssemblyApi;
  const statusEl = document.querySelector("#troubleshoot-status");
  const listEl = document.querySelector("#troubleshoot-list");
  const robotSelect = document.querySelector("#filter-robot");
  const groupSelect = document.querySelector("#filter-group");
  const searchInput = document.querySelector("#filter-search");
  let debounceTimer = 0;

  const robots = window.ROBOT_MODELS || [];
  const robotNames = new Map(robots.map((robot) => [robot.id, robot.name]));
  robots.forEach((robot) => {
    const option = document.createElement("option");
    option.value = robot.id;
    option.textContent = robot.name;
    robotSelect.append(option);
  });

  const requestedRobotId = new URLSearchParams(window.location.search).get("robotId");
  if (requestedRobotId && robotNames.has(requestedRobotId)) {
    robotSelect.value = requestedRobotId;
  }

  function renderGuide(guide) {
    const article = document.createElement("article");
    article.className = "troubleshoot-item";

    const heading = document.createElement("h2");
    heading.textContent = guide.symptom;

    const meta = document.createElement("p");
    meta.className = "troubleshoot-meta";
    const robotLabel = guide.robotId ? robotNames.get(guide.robotId) || guide.robotId : "Dùng chung nhiều mẫu";
    meta.textContent = `${robotLabel} · ${guide.componentGroup}`;

    const causesTitle = document.createElement("strong");
    causesTitle.textContent = "Nguyên nhân có thể:";
    const causes = document.createElement("p");
    causes.textContent = guide.possibleCauses;

    const stepsTitle = document.createElement("strong");
    stepsTitle.textContent = "Thứ tự kiểm tra và xử lý:";
    const steps = document.createElement("p");
    steps.className = "troubleshoot-steps";
    steps.textContent = guide.resolutionSteps;

    article.append(heading, meta, causesTitle, causes, stepsTitle, steps);

    if (guide.relatedComponentId) {
      const link = document.createElement("a");
      link.href = `linh-kien.html#${encodeURIComponent(guide.relatedComponentId)}`;
      link.textContent = `Xem linh kiện liên quan: ${guide.relatedComponentId} →`;
      article.append(link);
    }
    if (guide.robotId) {
      const link = document.createElement("a");
      link.href = `lap-rap.html?model=${encodeURIComponent(guide.robotId)}`;
      link.textContent = "Về trang chuẩn bị & lắp ráp mẫu này →";
      article.append(link);
    }

    return article;
  }

  async function search() {
    statusEl.textContent = "Đang tìm…";
    try {
      const { items, meta } = await api.troubleshooting.search({
        robotId: robotSelect.value || undefined,
        componentGroup: groupSelect.value || undefined,
        search: searchInput.value.trim() || undefined
      });

      if (groupSelect.children.length <= 1 && meta.componentGroups) {
        meta.componentGroups.forEach((group) => {
          const option = document.createElement("option");
          option.value = group;
          option.textContent = group;
          groupSelect.append(option);
        });
      }

      listEl.replaceChildren(...items.map(renderGuide));
      statusEl.textContent = items.length === 0
        ? "Không tìm thấy tình huống phù hợp."
        : `Tìm thấy ${meta.total} tình huống.`;
    } catch (error) {
      statusEl.textContent = `Không thể tải dữ liệu. ${error.message}`;
    }
  }

  robotSelect.addEventListener("change", () => void search());
  groupSelect.addEventListener("change", () => void search());
  searchInput.addEventListener("input", () => {
    window.clearTimeout(debounceTimer);
    debounceTimer = window.setTimeout(() => void search(), 300);
  });

  void search();
})();
