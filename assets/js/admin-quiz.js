"use strict";

(() => {
  const $ = (id) => document.getElementById(id);
  const api = window.RobotAssemblyApi;
  const MIN_OPTIONS = 2;
  const MAX_OPTIONS = 6;
  let selectedId = null;
  let questions = [];

  function status(message, state = "") {
    $("status").textContent = message;
    $("status").dataset.state = state;
  }

  function lockOut(message) {
    $("status").hidden = true;
    $("gate-message").textContent = message;
    $("admin-gate").hidden = false;
  }

  function optionLetter(index) {
    return String.fromCharCode(97 + index);
  }

  function addOptionRow(label = "", correct = false) {
    const row = document.createElement("div");
    row.className = "quiz-admin-option";

    const radio = document.createElement("input");
    radio.type = "radio";
    radio.name = "correct-option";
    radio.checked = correct;
    radio.setAttribute("aria-label", "Đáp án đúng");

    const text = document.createElement("input");
    text.type = "text";
    text.value = label;
    text.required = true;
    text.maxLength = 300;
    text.placeholder = "Nội dung lựa chọn";

    const remove = document.createElement("button");
    remove.type = "button";
    remove.className = "quiz-admin-remove";
    remove.textContent = "Xóa";
    remove.addEventListener("click", () => {
      if ($("options").children.length <= MIN_OPTIONS) {
        status(`Cần ít nhất ${MIN_OPTIONS} lựa chọn.`, "error");
        return;
      }
      row.remove();
    });

    row.append(radio, text, remove);
    $("options").append(row);
  }

  function renderForm(question) {
    selectedId = question?.id || null;
    $("editor-title").textContent = selectedId ? `Sửa: ${selectedId}` : "Thêm câu hỏi";
    $("delete").hidden = !selectedId;
    $("field-id").value = question?.id || "";
    $("field-id").disabled = Boolean(selectedId);
    $("field-order").value = question?.questionOrder || ($("robot-select").selectedOptions[0]
      ? questions.length + 1 : 1);
    $("field-prompt").value = question?.prompt || "";
    $("field-explanation").value = question?.explanation || "";
    $("options").replaceChildren();
    const options = question?.options?.length ? question.options : [{ label: "", correct: false }, { label: "", correct: false }];
    options.forEach((option) => addOptionRow(option.label, option.isCorrect));
    markSelected();
  }

  function markSelected() {
    for (const button of $("items").querySelectorAll("button[data-question-id]")) {
      button.setAttribute("aria-current", String(button.dataset.questionId === selectedId));
    }
  }

  function renderList() {
    $("items").replaceChildren();
    if (questions.length === 0) {
      const empty = document.createElement("li");
      empty.textContent = "Chưa có câu hỏi nào cho mẫu robot này.";
      $("items").append(empty);
      return;
    }
    questions.forEach((question) => {
      const li = document.createElement("li");
      const button = document.createElement("button");
      button.type = "button";
      button.dataset.questionId = question.id;
      button.textContent = `${question.questionOrder}. ${question.prompt}`;
      button.addEventListener("click", () => renderForm(question));
      li.append(button);
      $("items").append(li);
    });
    markSelected();
  }

  async function loadQuestions() {
    const robotId = $("robot-select").value;
    status("Đang tải câu hỏi…", "loading");
    try {
      questions = await api.adminQuiz.list(robotId);
      renderList();
      status("");
    } catch (error) {
      status(`Không thể tải câu hỏi. ${error.message}`, "error");
    }
  }

  function collectFormData() {
    const robotId = $("robot-select").value;
    const options = [...$("options").querySelectorAll(".quiz-admin-option")].map((row) => ({
      label: row.querySelector("input[type='text']").value.trim(),
      isCorrect: row.querySelector("input[type='radio']").checked
    }));
    return {
      id: $("field-id").value.trim(),
      robotId,
      prompt: $("field-prompt").value.trim(),
      explanation: $("field-explanation").value.trim(),
      questionOrder: $("field-order").value,
      options: options.map((option, index) => ({
        id: `${$("field-id").value.trim()}-${optionLetter(index)}`,
        label: option.label,
        isCorrect: option.isCorrect
      }))
    };
  }

  $("robot-select").addEventListener("change", () => {
    renderForm(null);
    void loadQuestions();
  });

  $("new-question").addEventListener("click", () => renderForm(null));

  $("add-option").addEventListener("click", () => {
    if ($("options").children.length >= MAX_OPTIONS) {
      status(`Tối đa ${MAX_OPTIONS} lựa chọn.`, "error");
      return;
    }
    addOptionRow();
  });

  $("editor").addEventListener("submit", async (event) => {
    event.preventDefault();
    const data = collectFormData();
    if (!data.options.some((option) => option.isCorrect)) {
      status("Hãy chọn đúng một lựa chọn đúng.", "error");
      return;
    }
    status("Đang lưu…", "loading");
    try {
      if (selectedId) {
        await api.adminQuiz.update(selectedId, data);
      } else {
        await api.adminQuiz.create(data);
      }
      status("Đã lưu câu hỏi.", "saved");
      await loadQuestions();
      renderForm(null);
    } catch (error) {
      status(`Không thể lưu câu hỏi. ${error.message}`, "error");
    }
  });

  $("delete").addEventListener("click", async () => {
    if (!selectedId) return;
    status("Đang xóa…", "loading");
    try {
      await api.adminQuiz.remove(selectedId);
      status("Đã xóa câu hỏi.", "saved");
      await loadQuestions();
      renderForm(null);
    } catch (error) {
      status(`Không thể xóa câu hỏi. ${error.message}`, "error");
    }
  });

  async function start() {
    let session;
    try {
      session = await api.auth.me();
    } catch (error) {
      lockOut(error.code === "AUTH_REQUIRED"
        ? "Cần đăng nhập bằng tài khoản quản trị."
        : `Không thể kiểm tra phiên đăng nhập. ${error.message}`);
      return;
    }
    if (session.role !== "ADMIN") {
      lockOut(`Tài khoản ${session.email} không có quyền quản trị.`);
      return;
    }

    const robots = window.ROBOT_MODELS || [];
    $("robot-select").replaceChildren(...robots.map((robot) => {
      const option = document.createElement("option");
      option.value = robot.id;
      option.textContent = robot.name;
      return option;
    }));

    $("status").hidden = true;
    $("workspace").hidden = false;
    $("workspace").disabled = false;
    renderForm(null);
    void loadQuestions();
  }

  void start();
})();
