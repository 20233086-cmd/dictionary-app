document.addEventListener("DOMContentLoaded", () => {
  renderHeader("home");
  loadWordOfDay();
  loadStats();
  loadTopViewed();
  setupSearch();

  const params = new URLSearchParams(window.location.search);
  const presetWord = params.get("word");
  if (presetWord) {
    searchInput.value = presetWord;
    doLookup(presetWord);
  }
});

const searchInput = document.getElementById("search-input");
const suggestBox = document.getElementById("search-suggest");
const resultArea = document.getElementById("result-area");
let lookupDirection = "en-vi"; // "en-vi" hoặc "vi-en"

function setupSearch() {
  document.getElementById("btn-search").addEventListener("click", () => runSearch(searchInput.value));

  searchInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter") {
      closeSuggest();
      runSearch(searchInput.value);
    }
  });

  let debounceTimer;
  searchInput.addEventListener("input", () => {
    clearTimeout(debounceTimer);
    const value = searchInput.value.trim();
    if (value.length < 1 || lookupDirection !== "en-vi") { closeSuggest(); return; }
    debounceTimer = setTimeout(() => fetchSuggestions(value), 220);
  });

  document.addEventListener("click", (e) => {
    if (!e.target.closest(".search-box")) closeSuggest();
  });

  document.querySelectorAll("[data-quick-word]").forEach((btn) => {
    btn.addEventListener("click", () => {
      const word = btn.getAttribute("data-quick-word");
      searchInput.value = word;
      runSearch(word);
    });
  });

  document.querySelectorAll("#lookup-toggle [data-direction]").forEach((btn) => {
    btn.addEventListener("click", () => {
      lookupDirection = btn.getAttribute("data-direction");
      document.querySelectorAll("#lookup-toggle [data-direction]").forEach((b) => b.classList.remove("is-active"));
      btn.classList.add("is-active");

      const quickWords = document.getElementById("quick-words");
      if (lookupDirection === "vi-en") {
        searchInput.placeholder = "Nhập nghĩa tiếng Việt, ví dụ: xinh đẹp...";
        if (quickWords) quickWords.style.display = "none";
      } else {
        searchInput.placeholder = "Nhập từ tiếng Anh, ví dụ: achieve...";
        if (quickWords) quickWords.style.display = "";
      }
      searchInput.value = "";
      closeSuggest();
    });
  });
}

function runSearch(value) {
  if (lookupDirection === "vi-en") {
    doReverseLookup(value);
  } else {
    doLookup(value);
  }
}

async function fetchSuggestions(prefix) {
  try {
    const suggestions = await Api.get("/words/suggest?prefix=" + encodeURIComponent(prefix));
    if (!suggestions.length) { closeSuggest(); return; }
    suggestBox.innerHTML = suggestions
      .map((s) => `<div class="search-suggest__item" data-word="${escapeHtml(s)}">${escapeHtml(s)}</div>`)
      .join("");
    suggestBox.classList.add("is-open");
    suggestBox.querySelectorAll(".search-suggest__item").forEach((item) => {
      item.addEventListener("click", () => {
        const word = item.getAttribute("data-word");
        searchInput.value = word;
        closeSuggest();
        doLookup(word);
      });
    });
  } catch (err) {
    closeSuggest();
  }
}

async function doReverseLookup(rawKeyword) {
  const keyword = (rawKeyword || "").trim();
  if (!keyword) return;

  resultArea.innerHTML = `<div class="card"><div class="loading-inline">Đang tìm từ tiếng Anh cho "${escapeHtml(keyword)}"...</div></div>`;

  try {
    const result = await Api.get(`/words/reverse-search?keyword=${encodeURIComponent(keyword)}&size=20`);
    if (!result.content.length) {
      renderNotFound(keyword);
      return;
    }
    resultArea.innerHTML = `
      <div class="card">
        <p class="side-card__label" style="margin-bottom:14px;">KẾT QUẢ CHO "${escapeHtml(keyword)}"</p>
        <div class="reverse-results">
          ${result.content.map((w) => {
            const match = w.meanings.find((m) => m.vietnamese.toLowerCase().includes(keyword.toLowerCase())) || w.meanings[0];
            return `
              <a href="#" class="reverse-result-item" data-word="${escapeHtml(w.english)}">
                <div class="reverse-result-item__en">${escapeHtml(w.english)}${w.phonetic ? ` <span style="font-weight:400; color:var(--color-ink-faint); font-size:14px;">${escapeHtml(w.phonetic)}</span>` : ""}</div>
                <div class="reverse-result-item__vi">${escapeHtml(match ? match.vietnamese : "")}</div>
              </a>
            `;
          }).join("")}
        </div>
      </div>
    `;
    resultArea.querySelectorAll("[data-word]").forEach((el) => {
      el.addEventListener("click", (e) => {
        e.preventDefault();
        const englishWord = el.getAttribute("data-word");
        document.querySelector('#lookup-toggle [data-direction="en-vi"]').click();
        searchInput.value = englishWord;
        doLookup(englishWord);
      });
    });
  } catch (err) {
    resultArea.innerHTML = `<div class="alert alert--error" style="display:block;">${escapeHtml(err.message)}</div>`;
  }
}

function closeSuggest() {
  suggestBox.classList.remove("is-open");
  suggestBox.innerHTML = "";
}

async function doLookup(rawWord) {
  const word = (rawWord || "").trim();
  if (!word) return;

  resultArea.innerHTML = `<div class="card"><div class="loading-inline">Đang tra cứu "${escapeHtml(word)}"...</div></div>`;

  try {
    const data = await Api.get("/words/lookup/" + encodeURIComponent(word));
    let favorited = false;
    let inDeck = false;
    if (Api.isLoggedIn()) {
      try { favorited = await Api.get(`/favorites/${data.id}/check`); } catch (e) { /* ignore */ }
      try { inDeck = await Api.get(`/flashcards/${data.id}/check`); } catch (e) { /* ignore */ }
    }
    renderEntry(data, favorited, inDeck);
    loadTopViewed();
  } catch (err) {
    renderNotFound(word);
  }
}

function renderNotFound(word) {
  resultArea.innerHTML = `
    <div class="card empty-state">
      <svg class="empty-state__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
        <circle cx="12" cy="12" r="10"></circle>
        <line x1="12" y1="8" x2="12" y2="12"></line>
        <line x1="12" y1="16" x2="12.01" y2="16"></line>
      </svg>
      <p class="empty-state__title">Không tìm thấy "${escapeHtml(word)}"</p>
      <p>Từ này chưa có trong từ điển. Bạn có thể hỏi Dictly Bot ở góc phải màn hình để được giải thích nhanh.</p>
    </div>
  `;
}

function renderEntry(word, favorited, inDeck) {
  const meaningsHtml = word.meanings.map((m) => `
    <li class="sense-item">
      <span class="sense-item__num"></span>
      <div class="sense-item__body">
        ${m.partOfSpeech ? `<p class="sense-item__pos">${escapeHtml(m.partOfSpeech)}</p>` : ""}
        <p class="sense-item__vi">${escapeHtml(m.vietnamese)}</p>
        ${m.exampleEn ? `
          <div class="sense-item__example">
            <p class="sense-item__example-en">${escapeHtml(m.exampleEn)}</p>
            ${m.exampleVi ? `<p class="sense-item__example-vi">${escapeHtml(m.exampleVi)}</p>` : ""}
          </div>` : ""}
        ${m.synonyms ? `<p class="sense-item__synonyms"><strong>Từ đồng nghĩa:</strong> ${escapeHtml(m.synonyms)}</p>` : ""}
      </div>
    </li>
  `).join("");

  resultArea.innerHTML = `
    <div class="card entry">
      <div class="entry__head">
        <div style="flex:1; min-width:0;">
          <h2 class="entry__headword">${escapeHtml(word.english)}</h2>
          ${word.phonetic ? `<p class="entry__phonetic">${escapeHtml(word.phonetic)}</p>` : ""}
        </div>
        <div class="entry__badges">
          ${word.level ? `<span class="badge badge--level">${escapeHtml(word.level)}</span>` : ""}
        </div>
        <div class="entry__actions">
          <button class="icon-btn" id="btn-speak" type="button" title="Phát âm">
            <svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polygon points="11 5 6 9 2 9 2 15 6 15 11 19 11 5"></polygon>
              <path d="M15.54 8.46a5 5 0 0 1 0 7.07"></path>
              <path d="M19.07 4.93a10 10 0 0 1 0 14.14"></path>
            </svg>
          </button>
          <button class="icon-btn ${favorited ? "icon-btn--active" : ""}" id="btn-favorite" type="button" title="Yêu thích">
            <svg width="17" height="17" viewBox="0 0 24 24" fill="${favorited ? "currentColor" : "none"}" stroke="currentColor" stroke-width="2">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
            </svg>
          </button>
          <button class="icon-btn ${inDeck ? "icon-btn--active" : ""}" id="btn-flashcard" type="button" title="Thêm vào bộ ôn tập">
            <svg width="17" height="17" viewBox="0 0 24 24" fill="${inDeck ? "currentColor" : "none"}" stroke="currentColor" stroke-width="2">
              <rect x="3" y="4" width="14" height="10" rx="2"></rect>
              <rect x="7" y="8" width="14" height="10" rx="2"></rect>
            </svg>
          </button>
        </div>
      </div>
      <ul class="sense-list">${meaningsHtml}</ul>
    </div>
  `;

  document.getElementById("btn-speak").addEventListener("click", () => speak(word.english));

  const favBtn = document.getElementById("btn-favorite");
  favBtn.addEventListener("click", async () => {
    if (!Api.isLoggedIn()) {
      window.location.href = "login.html?redirect=index.html";
      return;
    }
    try {
      if (favBtn.classList.contains("icon-btn--active")) {
        await Api.del(`/favorites/${word.id}`);
        favBtn.classList.remove("icon-btn--active");
        favBtn.querySelector("svg").setAttribute("fill", "none");
      } else {
        await Api.post(`/favorites/${word.id}`);
        favBtn.classList.add("icon-btn--active");
        favBtn.querySelector("svg").setAttribute("fill", "currentColor");
      }
    } catch (err) {
      alert(err.message);
    }
  });

  const deckBtn = document.getElementById("btn-flashcard");
  deckBtn.addEventListener("click", async () => {
    if (!Api.isLoggedIn()) {
      window.location.href = "login.html?redirect=index.html";
      return;
    }
    try {
      if (deckBtn.classList.contains("icon-btn--active")) {
        await Api.del(`/flashcards/${word.id}`);
        deckBtn.classList.remove("icon-btn--active");
        deckBtn.querySelector("svg").setAttribute("fill", "none");
      } else {
        await Api.post(`/flashcards/${word.id}`);
        deckBtn.classList.add("icon-btn--active");
        deckBtn.querySelector("svg").setAttribute("fill", "currentColor");
      }
    } catch (err) {
      alert(err.message);
    }
  });
}

function speak(text) {
  if (!("speechSynthesis" in window)) {
    alert("Trình duyệt của bạn không hỗ trợ phát âm.");
    return;
  }
  const utter = new SpeechSynthesisUtterance(text);
  utter.lang = "en-US";
  window.speechSynthesis.cancel();
  window.speechSynthesis.speak(utter);
}

async function loadWordOfDay() {
  const el = document.getElementById("wotd-content");
  try {
    const word = await Api.get("/words/word-of-day");
    const firstMeaning = word.meanings && word.meanings[0];
    el.innerHTML = `
      <p class="wotd__word">${escapeHtml(word.english)}</p>
      ${word.phonetic ? `<p class="wotd__phonetic">${escapeHtml(word.phonetic)}</p>` : ""}
      ${firstMeaning ? `<p class="wotd__meaning">${escapeHtml(firstMeaning.vietnamese)}</p>` : ""}
      <button class="btn btn--ghost btn--sm btn--block" data-word="${escapeHtml(word.english)}" id="btn-wotd-view">Xem chi tiết</button>
    `;
    document.getElementById("btn-wotd-view").addEventListener("click", () => {
      searchInput.value = word.english;
      doLookup(word.english);
      window.scrollTo({ top: 0, behavior: "smooth" });
    });
  } catch (err) {
    el.innerHTML = `<p class="field-hint">Chưa có dữ liệu từ vựng.</p>`;
  }
}

async function loadStats() {
  try {
    const stats = await Api.get("/words/stats");
    document.getElementById("stat-total").textContent = stats.totalWords;
  } catch (err) { /* ignore */ }
}

async function loadTopViewed() {
  const el = document.getElementById("top-viewed-content");
  try {
    const words = await Api.get("/words/most-viewed?limit=5");
    if (!words.length) { el.innerHTML = `<p class="field-hint">Chưa có dữ liệu.</p>`; return; }
    el.innerHTML = `<div class="row-list">${words.map((w) => `
      <div class="row-item" style="padding:9px 0;">
        <div class="row-item__main">
          <a href="#" class="row-item__word" data-word="${escapeHtml(w.english)}" style="font-size:15.5px;">${escapeHtml(w.english)}</a>
        </div>
      </div>
    `).join("")}</div>`;
    el.querySelectorAll("[data-word]").forEach((a) => {
      a.addEventListener("click", (e) => {
        e.preventDefault();
        const word = a.getAttribute("data-word");
        searchInput.value = word;
        doLookup(word);
        window.scrollTo({ top: 0, behavior: "smooth" });
      });
    });
  } catch (err) {
    el.innerHTML = `<p class="field-hint">Không tải được dữ liệu.</p>`;
  }
}
