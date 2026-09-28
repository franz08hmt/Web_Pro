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

  if (!api || !robotId) {
    statusEl.textContent = "Thiếu mẫu robot để làm bài. Hãy quay lại và chọn một mẫu robot.";
    return;
  }

  async function loadRobotName() {
    try {
      const models = (await window.RobotContentApi?.loadRobots()) || window.ROBOT_MODELS || [];
      const model = models.find((item) => item.id === robotId);
      robotNameEl.textContent = model ? model.name : robotId;
    } catch {
      robotNameEl.textContent = robotId;
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
    try {
      const data = await api.quiz.questions(robotId);
      if (data.questions.length === 0) {
        statusEl.textContent = "Mẫu robot này chưa có bài kiểm tra.";
        return;
      }
      renderQuestions(data.questions);
      statusEl.textContent = "";
      form.hidden = false;
    } catch (error) {
      statusEl.textContent = error.code === "AUTH_REQUIRED"
        ? "Đăng nhập để làm bài kiểm tra và lưu kết quả vào tài khoản."
        : `Không thể tải câu hỏi. ${error.message}`;
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

  void loadRobotName();
  void loadQuiz();
  void loadHistory();
})();
