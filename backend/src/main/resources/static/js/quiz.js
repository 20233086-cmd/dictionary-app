let quizQuestions = [];
let quizIndex = 0;
let quizScore = 0;

document.addEventListener("DOMContentLoaded", () => {
  renderHeader("quiz");
  if (!requireLogin("quiz.html")) return;
  renderStartScreen();
});

function renderStartScreen() {
  const area = document.getElementById("quiz-area");
  area.innerHTML = `
    <div class="card" style="text-align:center; padding: 46px 30px;">
      <p style="font-size:40px; margin-bottom:10px;">🧠</p>
      <p style="font-weight:700; font-size:19px; margin-bottom:8px;">Sẵn sàng kiểm tra trí nhớ?</p>
      <p class="page-subtitle" style="margin-bottom:24px;">Quiz sẽ ưu tiên các từ bạn cần ôn tập, sau đó tới từ yêu thích và từ ngẫu nhiên.</p>
      <button class="btn btn--primary" id="btn-start-quiz">Bắt đầu Quiz (10 câu)</button>
    </div>
  `;
  document.getElementById("btn-start-quiz").addEventListener("click", startQuiz);
}

async function startQuiz() {
  const area = document.getElementById("quiz-area");
  area.innerHTML = `<div class="loading-inline">Đang tạo câu hỏi...</div>`;

  try {
    quizQuestions = await Api.get("/quiz/daily?count=10");
    quizIndex = 0;
    quizScore = 0;
    if (!quizQuestions.length) {
      area.innerHTML = `<div class="alert alert--error" style="display:block;">Chưa đủ dữ liệu từ vựng để tạo quiz.</div>`;
      return;
    }
    renderQuestion();
  } catch (err) {
    area.innerHTML = `<div class="alert alert--error" style="display:block;">${escapeHtml(err.message)}</div>`;
  }
}

function renderQuestion() {
  const area = document.getElementById("quiz-area");

  if (quizIndex >= quizQuestions.length) {
    renderResult();
    return;
  }

  const q = quizQuestions[quizIndex];

  area.innerHTML = `
    <div class="quiz-progress">
      <span>Câu ${quizIndex + 1} / ${quizQuestions.length}</span>
      <span>Điểm: ${quizScore}</span>
    </div>
    <div class="quiz-question">
      <p class="quiz-question__prompt">${escapeHtml(q.english)}</p>
      ${q.phonetic ? `<p class="quiz-question__phonetic">${escapeHtml(q.phonetic)}</p>` : ""}
      <div class="quiz-options" id="quiz-options">
        ${q.options.map((opt, i) => `
          <button type="button" class="quiz-option" data-index="${i}">${escapeHtml(opt)}</button>
        `).join("")}
      </div>
    </div>
  `;

  document.querySelectorAll("#quiz-options .quiz-option").forEach((btn) => {
    btn.addEventListener("click", () => selectAnswer(parseInt(btn.getAttribute("data-index"), 10), q));
  });
}

function selectAnswer(selectedIndex, question) {
  const buttons = document.querySelectorAll("#quiz-options .quiz-option");
  buttons.forEach((btn) => (btn.disabled = true));

  const isCorrect = selectedIndex === question.correctIndex;
  if (isCorrect) quizScore++;

  buttons[question.correctIndex].classList.add("is-correct");
  if (!isCorrect) buttons[selectedIndex].classList.add("is-wrong");

  const area = document.getElementById("quiz-area");
  const nextBtn = document.createElement("button");
  nextBtn.className = "btn btn--primary btn--block";
  nextBtn.style.marginTop = "18px";
  nextBtn.textContent = quizIndex === quizQuestions.length - 1 ? "Xem kết quả" : "Câu tiếp theo →";
  nextBtn.addEventListener("click", () => {
    quizIndex++;
    renderQuestion();
  });
  area.querySelector(".quiz-question").appendChild(nextBtn);
}

function renderResult() {
  const area = document.getElementById("quiz-area");
  const total = quizQuestions.length;
  const percent = Math.round((quizScore / total) * 100);
  let comment = "Cố lên nhé, ôn thêm chút nữa!";
  if (percent >= 90) comment = "Xuất sắc! Bạn nhớ từ vựng rất tốt 🎉";
  else if (percent >= 70) comment = "Khá tốt! Chỉ còn vài từ cần ôn thêm.";
  else if (percent >= 50) comment = "Tạm ổn, hãy ôn lại các từ sai ở mục Flashcard.";

  area.innerHTML = `
    <div class="card quiz-result">
      <p class="quiz-result__score">${quizScore}/${total}</p>
      <p class="quiz-result__label">${comment}</p>
      <div style="display:flex; gap:10px; justify-content:center;">
        <button class="btn btn--primary" id="btn-retry-quiz">Làm lại</button>
        <a href="flashcards.html" class="btn btn--ghost">Ôn Flashcard</a>
      </div>
    </div>
  `;
  document.getElementById("btn-retry-quiz").addEventListener("click", renderStartScreen);
}
