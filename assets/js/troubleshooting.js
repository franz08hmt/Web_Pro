"use strict";

(() => {
  const api = window.RobotAssemblyApi;
  const statusEl = document.querySelector("#troubleshoot-status");
  const listEl = document.querySelector("#troubleshoot-list");
  const robotSelect = document.querySelector("#filter-robot");
  const groupSelect = document.querySelector("#filter-group");
  const searchInput = document.querySelector("#filter-search");
  let debounceTimer = 0;
  let searchVersion = 0;

  const robots = window.ROBOT_MODELS || [];
  const robotNames = new Map(robots.map((robot) => [robot.id, robot.name]));
  robots.forEach((robot) => {
    const option = document.createElement("option");
    option.value = robot.id;
    option.textContent = robot.name;
    robotSelect.append(option);
  });

  const requestedRobotId = new URLSearchParams(window.location.search).get("robotId");
  const requestedGroup = new URLSearchParams(window.location.search).get("componentGroup");
  searchInput.value = new URLSearchParams(window.location.search).get("search") || "";
  if (requestedRobotId && robotNames.has(requestedRobotId)) {
    robotSelect.value = requestedRobotId;
  }

  function renderGuide(guide) {
    const article = document.createElement("details");
    article.className = "troubleshoot-item";

    const heading = document.createElement("h2");
    heading.textContent = guide.symptom;
    const summary = document.createElement("summary");
    summary.append(heading);

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

    article.append(summary, meta, causesTitle, causes, stepsTitle, steps);

    if (guide.relatedComponentId) {
      const link = window.LearningContext.componentLink(guide.relatedComponentId);
      link.textContent = `Xem linh kiện liên quan: ${guide.relatedComponentId} →`;
      article.append(link);
    }
    if (guide.robotId) {
      const diagnosis = window.LearningContext.learnerLink("Luyện chẩn đoán cùng mẫu",
        `diagnosis?robotId=${encodeURIComponent(guide.robotId)}`);
      article.append(diagnosis);
      const link = document.createElement("a");
      link.href = `lap-rap.html?model=${encodeURIComponent(guide.robotId)}`;
      link.textContent = "Về trang chuẩn bị & lắp ráp mẫu này →";
      article.append(link);
    }

    return article;
  }

  async function search() {
    const version = ++searchVersion;
    statusEl.textContent = "Đang tìm…";
    listEl.replaceChildren();
    try {
      const { items, meta } = await api.troubleshooting.search({
        robotId: robotSelect.value || undefined,
        componentGroup: groupSelect.value || undefined,
        search: searchInput.value.trim() || undefined
      });
      if (version !== searchVersion) return;

      if (groupSelect.children.length <= 1 && meta.componentGroups) {
        meta.componentGroups.forEach((group) => {
          const option = document.createElement("option");
          option.value = group;
          option.textContent = group;
          groupSelect.append(option);
        });
        if (requestedGroup && meta.componentGroups.includes(requestedGroup)) {
          groupSelect.value = requestedGroup;
          void search();
          return;
        }
      }

      listEl.replaceChildren(...items.map(renderGuide));
      statusEl.textContent = items.length === 0
        ? "Không tìm thấy tình huống phù hợp."
        : `Tìm thấy ${meta.total} tình huống.`;
    } catch (error) {
      if (version !== searchVersion) return;
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
