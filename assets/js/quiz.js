"use strict";

(async () => {
  const params = new URLSearchParams(window.location.search);
  const robotId = params.get("model") || "";
  const api = window.RobotAssemblyApi;

  const robotNameEl = document.querySelector("#quiz-robot-name");
  const statusEl = document.querySelector("#quiz-status");
  const form = document.querySelector("#quiz-form");
  const questionsEl = document.querySelector("#quiz-questions");
  const submitButton = document.querySelector("#quiz-submit");
  const resultEl = document.querySelector("#quiz-result");
  const resultScoreEl = document.querySelector("#quiz-result-score");
  const resultAnswersEl = document.querySelector("#quiz-result-answers");
  const retryButton = document.querySelector("#quiz-retry");
  const historyStatusEl = document.querySelector("#quiz-history-status");
  const historyListEl = document.querySelector("#quiz-history-list");
  const selectionEl = document.querySelector("#quiz-selection");
  const loadRetry = document.querySelector("#quiz-load-retry");
  const historySection = document.querySelector("#quiz-history");

  if (!api) {
    robotNameEl.textContent = "Kiểm tra kiến thức";
    statusEl.textContent = "Chưa tải được công cụ làm bài. Hãy tải lại trang.";
    historySection.hidden = true;
    return;
  }

  async function showSelection() {
    robotNameEl.textContent = "Chọn bài kiểm tra kiến thức";
    document.title = "Chọn bài kiểm tra kiến thức | Robot Assembly Lab";
    historySection.hidden = true;
    historyStatusEl.textContent = "Chọn một mẫu để xem lịch sử của mẫu đó.";
    selectionEl.hidden = false;
    selectionEl.replaceChildren();
    loadRetry.hidden = true;
    statusEl.textContent = "Đang tải danh sách mẫu…";
    try {
      const models = await window.RobotContentApi.loadRobotOptions();
      if (!models?.length) {
        statusEl.textContent = "Chưa có mẫu robot để chọn bài kiểm tra.";
        return;
      }
      try { await api.auth.me(); }
      catch (error) {
        statusEl.textContent = error.status === 401
          ? "Đăng nhập để xem các bài kiểm tra hiện có và lưu kết quả của bạn."
          : "Chưa xác minh được tài khoản. Hãy thử tải lại danh sách.";
        const login = document.createElement("a");
        login.href = "tai-khoan.html";
        login.textContent = "Mở trang tài khoản";
        selectionEl.append(login);
        loadRetry.hidden = false;
        return;
      }
      let failed = false;
      // Reuse the existing read-only question API; its public JSON contains no answer key.
      const cards = await Promise.all(models.map(async model => {
        const card = document.createElement("article");
        card.className = "quiz-question";
        const name = document.createElement("h2");
        name.textContent = model.name;
        const state = document.createElement("p");
        card.append(name, state);
        try {
          const data = await api.quiz.questions(model.id);
          if (data.questions.length) {
            state.textContent = `${data.questions.length} câu hỏi hiện có.`;
            const link = document.createElement("a");
            link.href = `kiem-tra.html?model=${encodeURIComponent(model.id)}`;
            link.textContent = "Mở bài kiểm tra";
            card.append(link);
          } else state.textContent = "Mẫu này chưa có bài kiểm tra.";
        } catch {
          failed = true;
          state.textContent = "Chưa xác nhận được bài kiểm tra. Hãy thử tải lại danh sách.";
        }
        return card;
      }));
      selectionEl.replaceChildren(...cards);
      statusEl.textContent = "Chọn mẫu để mở bài; kết quả chỉ được lưu khi bạn nộp bài.";
      loadRetry.hidden = !failed;
    } catch {
      statusEl.textContent = "Không tải được danh sách mẫu. Hãy thử lại.";
      loadRetry.hidden = false;
    }
  }

  async function loadRobotName() {
    try {
      const models = (await window.RobotContentApi?.loadRobotOptions()) || [];
      const model = models.find((item) => item.id === robotId);
      robotNameEl.textContent = model ? model.name : "Bài kiểm tra kiến thức";
    } catch {
      robotNameEl.textContent = "Bài kiểm tra kiến thức";
    }
  }

  function renderQuestions(questions) {
    questionsEl.replaceChildren();
    questions.forEach((question, index) => {
      const card = document.createElement("fieldset");
      card.className = "quiz-question";
      card.dataset.questionId = question.id;

      const legend = document.createElement("legend");
      legend.textContent = `Câu ${index + 1}. ${question.prompt}`;
      card.append(legend);

      const optionList = document.createElement("div");
      optionList.className = "quiz-options";
      question.options.forEach((option) => {
        const label = document.createElement("label");
        label.className = "quiz-option";
        const input = document.createElement("input");
        input.type = "radio";
        input.name = `question-${question.id}`;
        input.value = option.id;
        input.required = true;
        const text = document.createElement("span");
        text.textContent = option.label;
        label.append(input, text);
        optionList.append(label);
      });
      card.append(optionList);
      questionsEl.append(card);
    });
  }

  function collectAnswers() {
    return [...questionsEl.querySelectorAll(".quiz-question")].map((card) => {
      const selected = card.querySelector("input[type='radio']:checked");
      return { questionId: card.dataset.questionId, optionId: selected ? selected.value : null };
    });
  }

  function renderResult(attempt) {
    form.hidden = true;
    resultEl.hidden = false;
    resultScoreEl.textContent = `Điểm của bạn: ${attempt.score}/${attempt.totalQuestions}`;
    resultAnswersEl.replaceChildren();
    attempt.answers.forEach((answer, index) => {
      const item = document.createElement("article");
      item.className = "quiz-answer";
      item.dataset.state = answer.isCorrect ? "correct" : "incorrect";

      const prompt = document.createElement("p");
      prompt.className = "quiz-answer-prompt";
      prompt.textContent = `Câu ${index + 1}. ${answer.questionPrompt}`;

      const chosen = document.createElement("p");
      chosen.textContent = `Bạn chọn: ${answer.selectedOptionLabel}`
        + (answer.isCorrect ? " — Đúng" : ` — Sai (đáp án đúng: ${answer.correctOptionLabel})`);

      const explanation = document.createElement("p");
      explanation.className = "quiz-answer-explanation";
      explanation.textContent = answer.explanation;

      item.append(prompt, chosen, explanation);
      resultAnswersEl.append(item);
    });
  }

  function formatDate(iso) {
    const date = new Date(iso);
    return Number.isNaN(date.getTime()) ? "" : date.toLocaleString("vi-VN");
  }

  async function loadHistory() {
    historyStatusEl.textContent = "Đang tải…";
    try {
      const { items, meta } = await api.quiz.history(robotId);
      historyListEl.replaceChildren();
      if (items.length === 0) {
        historyStatusEl.textContent = "Bạn chưa làm bài kiểm tra nào cho mẫu robot này.";
        return;
      }
      historyStatusEl.textContent = `Đã làm ${items.length}/${meta.total} lượt gần đây.`;
      items.forEach((attempt) => {
        const row = document.createElement("li");
        row.textContent = `${formatDate(attempt.submittedAt)} — ${attempt.score}/${attempt.totalQuestions} điểm`;
        historyListEl.append(row);
      });
    } catch (error) {
      historyStatusEl.textContent = `Không thể tải lịch sử. ${error.message}`;
    }
  }

  async function loadQuiz() {
    statusEl.textContent = "Đang tải câu hỏi…";
    form.hidden = true;
    resultEl.hidden = true;
    historySection.hidden = true;
    historyStatusEl.textContent = "Lịch sử hiện khi bài kiểm tra được tải thành công.";
    loadRetry.hidden = true;
    try {
      const data = await api.quiz.questions(robotId);
      if (data.questions.length === 0) {
        statusEl.textContent = "Mẫu robot này chưa có bài kiểm tra.";
        return;
      }
      renderQuestions(data.questions);
      statusEl.textContent = "";
      form.hidden = false;
      submitButton.disabled = false;
      historySection.hidden = false;
      void loadHistory();
    } catch (error) {
      statusEl.textContent = error.code === "AUTH_REQUIRED"
        ? "Đăng nhập để làm bài kiểm tra và lưu kết quả vào tài khoản."
        : error.status === 422 || error.status === 404
          ? "Không tìm thấy bài kiểm tra cho mẫu này. Hãy chọn lại bài."
          : "Không thể tải câu hỏi. Hãy thử lại hoặc chọn bài khác.";
      loadRetry.hidden = false;
    }
  }

  form.addEventListener("submit", async (event) => {
    event.preventDefault();
    const answers = collectAnswers();
    if (answers.some((answer) => !answer.optionId)) {
      statusEl.textContent = "Hãy chọn đáp án cho tất cả các câu trước khi nộp bài.";
      return;
    }

    submitButton.disabled = true;
    statusEl.textContent = "Đang nộp bài…";
    try {
      const attempt = await api.quiz.submit(robotId, answers);
      statusEl.textContent = "";
      renderResult(attempt);
      void loadHistory();
    } catch (error) {
      statusEl.textContent = `Không thể nộp bài. ${error.message}`;
      submitButton.disabled = false;
    }
  });

  retryButton.addEventListener("click", () => {
    resultEl.hidden = true;
    void loadQuiz();
  });

  loadRetry.addEventListener("click", () => void (robotId ? loadQuiz() : showSelection()));
  if (!robotId) {
    await showSelection();
    return;
  }
  void loadRobotName();
  void loadQuiz();
})();
