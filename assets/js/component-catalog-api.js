"use strict";

(() => {
  const api = window.RobotContentApi;
  const catalog = document.querySelector("#component-catalog");
  const status = document.querySelector("#component-data-status");
  const chart = document.querySelector(".chart-card");

  if (!api || !catalog) return;

  const make = (tag, className, text) => {
    const element = document.createElement(tag);
    if (className) element.className = className;
    if (text !== undefined) element.textContent = text;
    return element;
  };

  const normalize = value => String(value || "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLocaleLowerCase("vi")
    .replace(/đ/g, "d");

  function groupFor(category) {
    const value = normalize(category);
    if (value.includes("dieu khien") || value.includes("cong suat")) return "controller";
    if (value.includes("cam bien")) return "sensor";
    if (value.includes("dong co") || value.includes("co khi")) return "motion";
    return "utility";
  }

  function renderSpecs(specs) {
    const details = make("details", "spec-details");
    details.append(make("summary", "", "Thông số kỹ thuật"));
    const list = make("dl", "spec-list");

    for (const [label, value] of Object.entries(specs || {})) {
      list.append(make("dt", "", label), make("dd", "", String(value)));
    }

    details.append(list);
    return details;
  }

  function renderCard(component, usedBy) {
    const card = make("article", "card");
    card.dataset.componentId = component.id;
    card.dataset.componentCategory = groupFor(component.category);

    const figure = make("figure", "card-image");
    const image = make("img");
    Object.assign(image, {
      src: component.image,
      alt: component.name,
      width: 1024,
      height: 1024,
      loading: "lazy",
      decoding: "async"
    });
    figure.append(image);

    const usage = make("p", "component-use");
    usage.append(make("strong", "", "Dùng cho: "), usedBy || "chưa gán vào mẫu robot.");
    card.append(
      figure,
      make("p", "card-kicker", component.category),
      make("h2", "", component.name),
      make("p", "", component.description),
      usage,
      renderSpecs(component.specs)
    );
    return card;
  }

  function updateChart(components) {
    if (!chart) return;
    const counts = { controller: 0, sensor: 0, motion: 0, utility: 0 };
    components.forEach(component => { counts[groupFor(component.category)] += 1; });
    const max = Math.max(...Object.values(counts), 1);

    chart.querySelectorAll("[data-chart-group]").forEach(element => {
      const count = counts[element.dataset.chartGroup];
      if (element instanceof SVGRectElement) element.setAttribute("width", String(390 * count / max));
      else element.textContent = String(count);
    });
    chart.querySelector(".muted-text").textContent =
      `${components.length} linh kiện từ cơ sở dữ liệu được chia thành bốn nhóm chức năng.`;
    chart.querySelector("#chart-title").textContent =
      `Biểu đồ cột: bộ điều khiển ${counts.controller}; cảm biến ${counts.sensor}; chuyển động ${counts.motion}; nguồn và kết nối ${counts.utility} linh kiện.`;
    chart.querySelector("[data-chart-total]").textContent = String(components.length);
  }

  async function start() {
    if (!api.enabled) {
      if (status) status.textContent = "Đang hiển thị dữ liệu dự phòng trong chế độ xem tĩnh.";
      return;
    }

    try {
      const [components, robots] = await Promise.all([
        api.loadComponents(),
        api.loadRobots()
      ]);
      const usages = new Map();
      robots.forEach(robot => robot.parts.forEach(part => {
        const names = usages.get(part.id) || [];
        names.push(robot.name);
        usages.set(part.id, names);
      }));

      catalog.replaceChildren(...components.map(component =>
        renderCard(component, usages.get(component.id)?.join(", "))
      ));
      updateChart(components);
      document.dispatchEvent(new CustomEvent("robot:components-updated"));
      if (status) status.textContent = `Đã đồng bộ ${components.length} linh kiện từ Content API.`;
    } catch (error) {
      if (status) status.textContent = `Không thể đồng bộ API; đang dùng dữ liệu dự phòng. ${error.message}`;
    }
  }

  start();
})();
