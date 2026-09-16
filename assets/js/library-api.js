"use strict";

(() => {
  const api = window.RobotContentApi;
  const catalog = document.querySelector("#library-catalog");
  const status = document.querySelector("#library-data-status");

  if (!api || !catalog) return;

  const make = (tag, className, text) => {
    const element = document.createElement(tag);
    if (className) element.className = className;
    if (text !== undefined) element.textContent = text;
    return element;
  };

  function renderResource(resource) {
    const card = make("article", "card");
    if (resource.type === "image") {
      const figure = make("figure", "card-image");
      const image = make("img");
      Object.assign(image, {
        src: resource.url,
        alt: resource.title,
        width: 1024,
        height: 1024,
        loading: "lazy",
        decoding: "async"
      });
      figure.append(image);
      card.append(figure);
    }
    card.append(
      make("p", "card-kicker", resource.type === "image" ? "Hình ảnh mô hình" : "Tài liệu"),
      make("h2", "", resource.title),
      make("p", "", resource.description)
    );
    if (resource.type !== "image") {
      const link = make("a", "card-link", "Mở tài nguyên →");
      link.href = resource.url;
      card.append(link);
    }
    return card;
  }

  async function start() {
    if (!api.enabled) {
      if (status) status.textContent = "Đang hiển thị dữ liệu dự phòng trong chế độ xem tĩnh.";
      return;
    }

    try {
      const resources = await api.loadLibraryResources();
      catalog.replaceChildren(...resources.map(renderResource));
      if (status) status.textContent = `Đã đồng bộ ${resources.length} tài nguyên từ Content API.`;
    } catch (error) {
      if (status) status.textContent = `Không thể đồng bộ API; đang dùng dữ liệu dự phòng. ${error.message}`;
    }
  }

  start();
})();
