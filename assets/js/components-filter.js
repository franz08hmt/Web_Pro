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
  const cards = Array.from(
    catalog.querySelectorAll("[data-component-category]")
  );

  if (buttons.length === 0 || cards.length === 0) {
    return;
  }

  const applyFilter = (selectedFilter) => {
    buttons.forEach((button) => {
      const isActive = button.dataset.filter === selectedFilter;

      button.classList.toggle("is-active", isActive);
      button.setAttribute("aria-pressed", String(isActive));
    });

    cards.forEach((card) => {
      const isVisible =
        selectedFilter === "all" ||
        card.dataset.componentCategory === selectedFilter;

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

  const initialButton =
    buttons.find((button) => button.getAttribute("aria-pressed") === "true") ||
    buttons[0];

  applyFilter(initialButton.dataset.filter);
})();
