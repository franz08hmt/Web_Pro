"use strict";

// Dựng trạng thái để xem từ sự kiện server đã chấp nhận; không chấm hoặc gửi thao tác.
window.assemblyReplayFrames = function (layout, events) {
  const installed = new Map();
  const frame = () => ({...layout, canEdit: false, placements: [...installed.values()].map(item => ({...item}))});
  const frames = [frame()];
  for (const event of events) {
    if (event.action === "RESET") installed.clear();
    else if (event.action === "DETACH" || (event.action === "UNDO" && event.orientation === null)) {
      installed.delete(String(event.instanceId));
    } else if (event.action === "ATTACH" || (event.action === "UNDO" && event.orientation !== null)) {
      installed.set(String(event.instanceId), {instanceId: String(event.instanceId), slotId: String(event.slotId),
        orientation: event.orientation, placedAt: event.createdAtLabel});
    }
    frames.push(frame());
  }
  return frames;
};

window.createAssemblyReplay = function ({layout, events, view, panel}) {
  const frames = window.assemblyReplayFrames(layout, events);
  let index = 0;
  let timer = null;
  const reduced = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  const heading = document.createElement("h3");
  heading.textContent = "Xem lại quá trình lắp";
  const status = document.createElement("p");
  status.setAttribute("role", "status");
  status.setAttribute("aria-live", "polite");
  const controls = document.createElement("div");
  const button = (name, callback) => {
    const node = document.createElement("button");
    node.type = "button"; node.textContent = name; node.addEventListener("click", callback);
    controls.append(node); return node;
  };
  const pause = () => { clearInterval(timer); timer = null; };
  const draw = next => {
    index = Math.max(0, Math.min(events.length, next));
    view.sync(frames[index]);
    const event = events[index - 1];
    status.textContent = index ? `Bước ${index}/${events.length}: ${event.actionLabel}`
      + (event.instanceLabel ? ` ${event.instanceLabel} → ${event.slotLabel} (hướng ${event.orientationLabel})` : "")
      + (event.undone ? " — thao tác này đã được hoàn tác." : ".") : `Bắt đầu từ mô hình trống · ${events.length} thao tác.`;
    previous.disabled = index === 0;
    nextButton.disabled = index === events.length;
    if (index === events.length) pause();
  };
  const play = button("Phát", () => {
    if (reduced || timer || !events.length) return;
    if (index === events.length) draw(0);
    timer = setInterval(() => draw(index + 1), 1000);
  });
  button("Tạm dừng", pause);
  const previous = button("Bước trước", () => { pause(); draw(index - 1); });
  const nextButton = button("Bước sau", () => { pause(); draw(index + 1); });
  play.disabled = reduced || !events.length;
  const help = document.createElement("p");
  help.textContent = reduced ? "Đã giảm chuyển động. Bấm từng bước để xem; mô hình đã lưu không thay đổi."
    : "Chỉ xem lại thao tác đã lưu. Phát lại không sửa phiên hoặc gửi yêu cầu ghi dữ liệu.";
  const back = document.createElement("a");
  back.href = `../assembly-review?session=${encodeURIComponent(layout.sessionId)}`;
  back.textContent = "Về trang hoàn thiện";
  panel.prepend(heading, help, controls, status, back);
  if (!events.length) help.textContent = "Phiên này được tạo trước khi có nhật ký; không có dữ liệu phát lại.";
  draw(0);
  window.addEventListener("pagehide", pause);
  return {dispose: () => { pause(); window.removeEventListener("pagehide", pause); }};
};
