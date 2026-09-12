let dueCards = [];
let currentCardIndex = 0;

document.addEventListener("DOMContentLoaded", () => {
  renderHeader("flashcards");
  if (!requireLogin("flashcards.html")) return;
  loadDueCards();
});

async function loadDueCards() {
  const area = document.getElementById("flashcard-area");
  area.innerHTML = `<div class="loading-inline">Đang tải bộ thẻ...</div>`;

  try {
    dueCards = await Api.get("/flashcards/due?limit=20");
    currentCardIndex = 0;
    renderCard();
  } catch (err) {
    area.innerHTML = `<div class="alert alert--error" style="display:block;">${escapeHtml(err.message)}</div>`;
  }
}

function renderCard() {
  const area = document.getElementById("flashcard-area");

  if (dueCards.length === 0) {
    area.innerHTML = `
      <div class="card empty-state">
        <svg class="empty-state__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <rect x="3" y="4" width="14" height="10" rx="2"></rect>
          <rect x="7" y="8" width="14" height="10" rx="2"></rect>
        </svg>
        <p class="empty-state__title">Bộ ôn tập đang trống</p>
        <p>Hãy tra một từ ở <a href="index.html">trang chủ</a> rồi bấm biểu tượng thẻ nhớ (bên cạnh trái tim yêu thích) để thêm từ vào đây.</p>
      </div>
    `;
    return;
  }

  if (currentCardIndex >= dueCards.length) {
    area.innerHTML = `
      <div class="quiz-result">
        <p style="font-size: 46px; margin-bottom: 6px;">🎉</p>
        <p class="quiz-result__label" style="font-size:18px; font-weight:700; color:var(--color-ink);">
          Đã ôn xong ${dueCards.length} thẻ đến hạn hôm nay!
        </p>
        <p class="quiz-result__label">Quay lại vào ngày mai để ôn tiếp theo lịch, hoặc thử sức với Quiz.</p>
        <div style="display:flex; gap:10px; justify-content:center; margin-top:10px;">
          <a href="quiz.html" class="btn btn--primary">Làm Quiz ngay</a>
          <a href="index.html" class="btn btn--ghost">Về trang chủ</a>
        </div>
      </div>
    `;
    return;
  }

  const progress = dueCards.length ? (currentCardIndex / dueCards.length) * 100 : 0;
  const card = dueCards[currentCardIndex];
  const word = card.word;
  const firstMeaning = word.meanings && word.meanings[0];

  area.innerHTML = `
    <div class="flashcard-progress-bar">
      <div class="flashcard-progress-bar__fill" style="width:${progress}%"></div>
    </div>
    <div class="flashcard-stage">
      <div class="flashcard" id="flip-card">
        <p class="flashcard__label">Thẻ ${currentCardIndex + 1} / ${dueCards.length}</p>
        <p class="flashcard__word">${escapeHtml(word.english)}</p>
        ${word.phonetic ? `<p class="flashcard__phonetic">${escapeHtml(word.phonetic)}</p>` : ""}
        <div id="card-answer" style="display:none;">
          <p class="flashcard__meaning">${firstMeaning ? escapeHtml(firstMeaning.vietnamese) : ""}</p>
        </div>
        <p class="flashcard__hint" id="flip-hint">👆 Bấm vào thẻ để xem nghĩa</p>
      </div>
      <div class="flashcard-grades" id="grade-buttons" style="display:none;">
        <button type="button" class="flashcard-grades__again" data-quality="1">😵 Chưa nhớ</button>
        <button type="button" class="flashcard-grades__good" data-quality="3">🙂 Nhớ</button>
        <button type="button" class="flashcard-grades__easy" data-quality="5">🚀 Dễ</button>
      </div>
    </div>
  `;

  document.getElementById("flip-card").addEventListener("click", flipCard);
  document.querySelectorAll("#grade-buttons button").forEach((btn) => {
    btn.addEventListener("click", (e) => {
      e.stopPropagation();
      gradeCard(word.id, parseInt(btn.getAttribute("data-quality"), 10));
    });
  });
}

function flipCard() {
  const answer = document.getElementById("card-answer");
  const hint = document.getElementById("flip-hint");
  const grades = document.getElementById("grade-buttons");
  if (answer.style.display === "block") return;
  answer.style.display = "block";
  hint.style.display = "none";
  grades.style.display = "flex";
}

async function gradeCard(wordId, quality) {
  try {
    await Api.post(`/flashcards/review/${wordId}`, { quality });
    currentCardIndex++;
    renderCard();
  } catch (err) {
    alert(err.message);
  }
}
