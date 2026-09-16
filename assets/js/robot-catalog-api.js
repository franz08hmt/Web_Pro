"use strict";

(() => {
  const api = window.RobotContentApi;
  const catalog = document.querySelector("#robot-catalog");
  const compareBody = document.querySelector("#compare-body");
  const searchInput = document.querySelector("#robot-search");
  const emptyState = document.querySelector("#robot-empty");
  const status = document.querySelector("#robot-data-status");

  if (!api || !catalog || !compareBody || !searchInput || !emptyState) return;

  const make = (tag, className, text) => {
    const element = document.createElement(tag);
    if (className) element.className = className;
    if (text !== undefined) element.textContent = text;
    return element;
  };

  function renderCatalog(models) {
    const cards = models.map(model => {
      const card = make("article", "card robot-card");
      const figure = make("figure", "media-placeholder card-image");
      const image = make("img");
      Object.assign(image, {
        src: model.image,
        alt: model.name,
        width: 1024,
        height: 1024,
        loading: "lazy",
        decoding: "async"
      });
      figure.append(image);
      const facts = make("p");
      facts.append(
        make("strong", "", String(model.parts.length)),
        " nhóm linh kiện · ",
        make("strong", "", String(model.steps.length)),
        " bước · ",
        make("strong", "", model.buildTime || "—")
      );
      const link = make("a", "card-link", "Chọn mẫu này →");
      link.href = `lap-rap.html?model=${encodeURIComponent(model.id)}`;
      card.append(
        figure,
        make("p", "card-kicker", model.level),
        make("h2", "", model.name),
        make("p", "", model.summary),
        facts,
        link
      );
      return card;
    });

    catalog.replaceChildren(...cards);
    emptyState.hidden = models.length !== 0;
  }

  function renderComparison(models) {
    compareBody.replaceChildren(...models.map(model => {
      const row = make("tr");
      const name = make("th", "", model.name);
      name.scope = "row";
      for (const value of [
        model.level,
        model.parts.length,
        model.steps.length,
        model.buildTime || "—",
        model.mainSensor || "—",
        model.skills || "—"
      ]) row.append(make("td", "", String(value)));
      row.prepend(name);
      return row;
    }));
  }

  async function start() {
    if (!api.enabled) {
      if (status) status.textContent = "Đang hiển thị dữ liệu dự phòng trong chế độ xem tĩnh.";
      return;
    }

    try {
      const models = await api.loadRobots();
      renderCatalog(models);
      renderComparison(models);
      searchInput.addEventListener("input", () => {
        const keyword = searchInput.value.trim().toLocaleLowerCase("vi");
        renderCatalog(models.filter(model =>
          `${model.name} ${model.summary} ${model.level}`.toLocaleLowerCase("vi").includes(keyword)
        ));
      });
      if (status) status.textContent = `Đã đồng bộ ${models.length} mẫu robot từ Content API.`;
    } catch (error) {
      if (status) status.textContent = `Không thể đồng bộ API; đang dùng dữ liệu dự phòng. ${error.message}`;
    }
  }

  start();
})();
