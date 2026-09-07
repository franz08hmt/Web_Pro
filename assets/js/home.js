"use strict";

(() => {
    const root = document.querySelector("#featured-robots");
    const models = Array.isArray(window.ROBOT_MODELS)
        ? window.ROBOT_MODELS
        : [];

    if (!root || models.length === 0) return;

    const escapeHtml = (value) =>
        String(value).replace(/[&<>"']/g, (character) => ({
            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#039;"
        })[character]);

    root.innerHTML = models.map((model, index) => {
        const highlights = model.parts.slice(0, 3).map((part) => `
      <li>${escapeHtml(part.name)} <span>×${part.quantity}</span></li>
    `).join("");

        return `
      <article class="featured-card">
        <div class="featured-card-top">
          <span class="difficulty-tag">${escapeHtml(model.level)}</span>
          <span class="model-index">0${index + 1}</span>
        </div>

        <h3>${escapeHtml(model.name)}</h3>
        <p>${escapeHtml(model.summary)}</p>

        <div class="featured-meta">
          <span>${model.parts.length} linh kiện</span>
          <span>${model.steps.length} bước</span>
        </div>

        <ul class="featured-points">
          ${highlights}
        </ul>

        <a class="featured-action"
           href="pages/lap-rap.html?model=${encodeURIComponent(model.id)}">
          Lắp ráp mẫu này <span aria-hidden="true">→</span>
        </a>
      </article>
    `;
    }).join("");
})();