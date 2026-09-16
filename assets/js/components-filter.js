"use strict";

(() => {
  const controls = document.querySelector(".filter-controls");
  const catalog = document.querySelector("#component-catalog");

  if (!controls || !catalog) {
    return;
  }

  const buttons = Array.from(
    controls.querySelectorAll(".filter-button[data-filter]")
  );
  let cards = Array.from(catalog.querySelectorAll("[data-component-category]"));

  if (buttons.length === 0) {
    return;
  }

  let selectedFilter =
    buttons.find((button) => button.getAttribute("aria-pressed") === "true")?.dataset.filter ||
    buttons[0].dataset.filter;

  const updateCounts = () => {
    buttons.forEach((button) => {
      const count = button.dataset.filter === "all"
        ? cards.length
        : cards.filter(card => card.dataset.componentCategory === button.dataset.filter).length;
      const label = button.querySelector("span");
      if (label) label.textContent = String(count).padStart(2, "0");
    });
  };

  const applyFilter = (nextFilter) => {
    selectedFilter = nextFilter;
    buttons.forEach((button) => {
      const isActive = button.dataset.filter === nextFilter;

      button.classList.toggle("is-active", isActive);
      button.setAttribute("aria-pressed", String(isActive));
    });

    cards.forEach((card) => {
      const isVisible =
        nextFilter === "all" ||
        card.dataset.componentCategory === nextFilter;

      card.hidden = !isVisible;
    });
  };

  controls.addEventListener("click", (event) => {
    if (!(event.target instanceof Element)) {
      return;
    }

    const button = event.target.closest(".filter-button[data-filter]");

    if (!button || !controls.contains(button)) {
      return;
    }

    applyFilter(button.dataset.filter);
  });

  document.addEventListener("robot:components-updated", () => {
    cards = Array.from(catalog.querySelectorAll("[data-component-category]"));
    updateCounts();
    applyFilter(selectedFilter);
  });

  updateCounts();
  applyFilter(selectedFilter);
})();
