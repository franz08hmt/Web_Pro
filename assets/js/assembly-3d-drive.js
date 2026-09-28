"use strict";

(() => {
  const KEY_ACTIONS = Object.freeze({
    KeyW: Object.freeze({ move: 1, turn: 0 }),
    ArrowUp: Object.freeze({ move: 1, turn: 0 }),
    KeyS: Object.freeze({ move: -1, turn: 0 }),
    ArrowDown: Object.freeze({ move: -1, turn: 0 }),
    KeyA: Object.freeze({ move: 0, turn: 1 }),
    ArrowLeft: Object.freeze({ move: 0, turn: 1 }),
    KeyD: Object.freeze({ move: 0, turn: -1 }),
    ArrowRight: Object.freeze({ move: 0, turn: -1 })
  });

  function createAssemblyDriveController(options) {
    const {
      robotGroup,
      getFootprintRadius,
      canDrive,
      reducedMotion,
      platformRadius = 3.25,
      moveSpeed = 2.4,
      turnSpeed = 1.8,
      moveStep = 0.24,
      turnStep = Math.PI / 18
    } = options || {};

    if (!robotGroup?.position || typeof robotGroup.translateZ !== "function") {
      throw new TypeError("Assembly drive requires a Three.js robot group.");
    }
    if (typeof canDrive !== "function" || typeof getFootprintRadius !== "function") {
      throw new TypeError("Assembly drive requires session and footprint checks.");
    }

    const pressedKeys = new Set();
    let active = false;
    let lastFrameTime = null;

    function clearPressedKeys() {
      pressedKeys.clear();
      lastFrameTime = null;
    }

    function isEditableTarget(target) {
      const candidate = target || document.activeElement;
      return Boolean(candidate?.isContentEditable
        || candidate?.matches?.("input, textarea, select, [contenteditable='true']")
        || candidate?.closest?.("input, textarea, select, [contenteditable='true']"));
    }

    function clampToPlatform() {
      const rawFootprint = Number(getFootprintRadius());
      const footprintRadius = Number.isFinite(rawFootprint) ? Math.max(0, rawFootprint) : 0;
      const maxCenterRadius = Math.max(0, platformRadius - footprintRadius);
      const distance = Math.hypot(robotGroup.position.x, robotGroup.position.z);
      if (distance <= maxCenterRadius || distance === 0) return;

      const ratio = maxCenterRadius / distance;
      robotGroup.position.x *= ratio;
      robotGroup.position.z *= ratio;
    }

    function applyAction(action, distance, angle) {
      if (action.move) robotGroup.translateZ(-action.move * distance);
      if (action.turn) robotGroup.rotation.y += action.turn * angle;
      clampToPlatform();
    }

    function onKeyDown(event) {
      if (!active) return;
      const action = KEY_ACTIONS[event.code];
      if (!action) return;
      if (!canDrive()) {
        stop();
        return;
      }
      if (isEditableTarget(event.target)) return;

      event.preventDefault();
      if (reducedMotion?.matches) {
        if (!event.repeat) applyAction(action, moveStep, turnStep);
        return;
      }
      pressedKeys.add(event.code);
    }

    function onKeyUp(event) {
      pressedKeys.delete(event.code);
    }

    function onWindowBlur() {
      clearPressedKeys();
    }

    function onVisibilityChange() {
      if (document.hidden) clearPressedKeys();
    }

    function onPageHide() {
      destroy();
    }

    function start() {
      if (active) return true;
      if (!canDrive()) return false;

      active = true;
      clearPressedKeys();
      window.addEventListener("keydown", onKeyDown);
      window.addEventListener("keyup", onKeyUp);
      window.addEventListener("blur", onWindowBlur);
      document.addEventListener("visibilitychange", onVisibilityChange);
      window.addEventListener("pagehide", onPageHide);
      return true;
    }

    function stop() {
      if (!active) return;
      active = false;
      clearPressedKeys();
      window.removeEventListener("keydown", onKeyDown);
      window.removeEventListener("keyup", onKeyUp);
      window.removeEventListener("blur", onWindowBlur);
      document.removeEventListener("visibilitychange", onVisibilityChange);
      window.removeEventListener("pagehide", onPageHide);
    }

    function destroy() {
      stop();
    }

    function update(now) {
      if (!active) return;
      if (!canDrive()) {
        stop();
        return;
      }
      if (reducedMotion?.matches || pressedKeys.size === 0) {
        lastFrameTime = null;
        return;
      }

      if (!Number.isFinite(now)) return;
      if (lastFrameTime === null) {
        lastFrameTime = now;
        return;
      }

      const elapsed = Math.min(Math.max((now - lastFrameTime) / 1000, 0), 0.05);
      lastFrameTime = now;
      if (elapsed === 0) return;

      let forward = 0;
      let turn = 0;
      for (const code of pressedKeys) {
        const action = KEY_ACTIONS[code];
        forward += action.move;
        turn += action.turn;
      }
      if (forward) robotGroup.translateZ(-forward * moveSpeed * elapsed);
      if (turn) robotGroup.rotation.y += turn * turnSpeed * elapsed;
      clampToPlatform();
    }

    return Object.freeze({
      start,
      stop,
      update,
      destroy,
      isActive: () => active
    });
  }

  window.AssemblyDrive = Object.freeze({ create: createAssemblyDriveController });
})();
