let currentPage = 0;
const PAGE_SIZE = 10;
let currentKeyword = "";
let meaningCounter = 0;

document.addEventListener("DOMContentLoaded", () => {
  renderHeader("admin");

  if (!Api.isLoggedIn()) {
    window.location.href = "login.html?redirect=admin.html";
    return;
  }
  if (!Api.isAdmin()) {
    alert("Bạn không có quyền truy cập trang quản trị.");
    window.location.href = "index.html";
    return;
  }

  loadWords();

  let debounceTimer;
  document.getElementById("admin-search").addEventListener("input", (e) => {
    clearTimeout(debounceTimer);
    debounceTimer = setTimeout(() => {
      currentKeyword = e.target.value.trim();
      currentPage = 0;
      loadWords();
    }, 300);
  });

  document.getElementById("btn-new-word").addEventListener("click", () => openModal());
  document.getElementById("btn-cancel-modal").addEventListener("click", closeModal);
  document.getElementById("word-modal-overlay").addEventListener("click", (e) => {
    if (e.target.id === "word-modal-overlay") closeModal();
  });
  document.getElementById("btn-add-meaning").addEventListener("click", () => addMeaningBlock());
  document.getElementById("word-form").addEventListener("submit", submitWordForm);
});

async function loadWords() {
  const wrap = document.getElementById("admin-table-wrap");
  wrap.innerHTML = `<div class="loading-inline">Đang tải...</div>`;

  try {
    const query = `/admin/words?page=${currentPage}&size=${PAGE_SIZE}` +
      (currentKeyword ? `&keyword=${encodeURIComponent(currentKeyword)}` : "");
    const result = await Api.get(query);

    if (!result.content.length) {
      wrap.innerHTML = `<div class="empty-state"><p class="empty-state__title">Không có từ vựng nào</p></div>`;
      renderPagination(result);
      return;
    }

    wrap.innerHTML = `
      <table class="admin-table">
        <thead>
          <tr>
            <th>Từ</th>
            <th>Phiên âm</th>
            <th>Cấp độ</th>
            <th>Số nghĩa</th>
            <th>Lượt tra</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          ${result.content.map((w) => `
            <tr>
              <td><strong>${escapeHtml(w.english)}</strong></td>
              <td>${escapeHtml(w.phonetic || "—")}</td>
              <td>${w.level ? `<span class="badge badge--level">${escapeHtml(w.level)}</span>` : "—"}</td>
              <td>${w.meanings.length}</td>
              <td>${w.viewCount}</td>
              <td style="text-align:right; white-space:nowrap;">
                <button class="btn btn--ghost btn--sm" data-edit="${w.id}">Sửa</button>
                <button class="btn btn--danger btn--sm" data-delete="${w.id}" data-word="${escapeHtml(w.english)}">Xoá</button>
              </td>
            </tr>
          `).join("")}
        </tbody>
      </table>
    `;

    wrap.querySelectorAll("[data-edit]").forEach((btn) => {
      btn.addEventListener("click", () => openModal(btn.getAttribute("data-edit")));
    });
    wrap.querySelectorAll("[data-delete]").forEach((btn) => {
      btn.addEventListener("click", () => deleteWord(btn.getAttribute("data-delete"), btn.getAttribute("data-word")));
    });

    renderPagination(result);
  } catch (err) {
    wrap.innerHTML = `<div class="alert alert--error" style="display:block;">${escapeHtml(err.message)}</div>`;
  }
}

function renderPagination(result) {
  const el = document.getElementById("admin-pagination");
  if (result.totalPages <= 1) { el.innerHTML = ""; return; }

  let html = "";
  for (let i = 0; i < result.totalPages; i++) {
    html += `<button class="btn btn--sm ${i === result.page ? "btn--primary" : "btn--ghost"}" data-page="${i}">${i + 1}</button>`;
  }
  el.innerHTML = html;
  el.querySelectorAll("[data-page]").forEach((btn) => {
    btn.addEventListener("click", () => {
      currentPage = parseInt(btn.getAttribute("data-page"), 10);
      loadWords();
    });
  });
}

async function deleteWord(id, word) {
  if (!confirm(`Xoá từ "${word}" khỏi từ điển? Hành động này không thể hoàn tác.`)) return;
  try {
    await Api.del(`/admin/words/${id}`);
    loadWords();
  } catch (err) {
    alert(err.message);
  }
}

// ---------------- Modal thêm/sửa ----------------

async function openModal(wordId) {
  document.getElementById("word-form").reset();
  document.getElementById("modal-error").style.display = "none";
  document.getElementById("meanings-container").innerHTML = "";
  meaningCounter = 0;
  document.getElementById("word-id").value = wordId || "";

  if (wordId) {
    document.getElementById("modal-title").textContent = "Sửa từ vựng";
    try {
      const word = await Api.get(`/words/${wordId}`);
      document.getElementById("word-english").value = word.english;
      document.getElementById("word-phonetic").value = word.phonetic || "";
      document.getElementById("word-level").value = word.level || "";
      document.getElementById("word-topic").value = word.topic || "";
      word.meanings.forEach((m) => addMeaningBlock(m));
    } catch (err) {
      alert(err.message);
      return;
    }
  } else {
    document.getElementById("modal-title").textContent = "Thêm từ mới";
    addMeaningBlock();
  }

  document.getElementById("word-modal-overlay").classList.add("is-open");
}

function closeModal() {
  document.getElementById("word-modal-overlay").classList.remove("is-open");
}

function addMeaningBlock(data) {
  meaningCounter++;
  const id = "meaning-" + meaningCounter;
  const container = document.getElementById("meanings-container");

  const div = document.createElement("div");
  div.className = "meaning-block";
  div.dataset.blockId = id;
  div.innerHTML = `
    <button type="button" class="meaning-block__remove" data-remove-meaning>Xoá nghĩa ✕</button>
    <div style="display:grid; grid-template-columns: 1fr 2fr; gap: 14px;">
      <div class="field" style="margin-bottom:12px;">
        <label>Từ loại</label>
        <input type="text" class="m-pos" placeholder="noun, verb..." value="${escapeAttr(data?.partOfSpeech)}">
      </div>
      <div class="field" style="margin-bottom:12px;">
        <label>Nghĩa tiếng Việt *</label>
        <input type="text" class="m-vi" required value="${escapeAttr(data?.vietnamese)}">
      </div>
    </div>
    <div class="field" style="margin-bottom:12px;">
      <label>Ví dụ (Anh)</label>
      <input type="text" class="m-ex-en" value="${escapeAttr(data?.exampleEn)}">
    </div>
    <div class="field" style="margin-bottom:12px;">
      <label>Ví dụ (Việt)</label>
      <input type="text" class="m-ex-vi" value="${escapeAttr(data?.exampleVi)}">
    </div>
    <div class="field" style="margin-bottom:0;">
      <label>Từ đồng nghĩa (phân cách bởi dấu phẩy)</label>
      <input type="text" class="m-synonyms" value="${escapeAttr(data?.synonyms)}">
    </div>
  `;
  container.appendChild(div);

  div.querySelector("[data-remove-meaning]").addEventListener("click", () => {
    if (container.children.length <= 1) {
      alert("Từ vựng cần ít nhất một nghĩa.");
      return;
    }
    div.remove();
  });
}

async function submitWordForm(e) {
  e.preventDefault();
  const errorBox = document.getElementById("modal-error");
  const saveBtn = document.getElementById("btn-save-word");
  errorBox.style.display = "none";

  const meaningBlocks = document.querySelectorAll("#meanings-container .meaning-block");
  const meanings = Array.from(meaningBlocks).map((block, index) => ({
    partOfSpeech: block.querySelector(".m-pos").value.trim(),
    vietnamese: block.querySelector(".m-vi").value.trim(),
    exampleEn: block.querySelector(".m-ex-en").value.trim(),
    exampleVi: block.querySelector(".m-ex-vi").value.trim(),
    synonyms: block.querySelector(".m-synonyms").value.trim(),
    orderIndex: index + 1,
  }));

  const payload = {
    english: document.getElementById("word-english").value.trim(),
    phonetic: document.getElementById("word-phonetic").value.trim(),
    level: document.getElementById("word-level").value,
    topic: document.getElementById("word-topic").value,
    meanings,
  };

  const wordId = document.getElementById("word-id").value;

  saveBtn.disabled = true;
  saveBtn.textContent = "Đang lưu...";

  try {
    if (wordId) {
      await Api.put(`/admin/words/${wordId}`, payload);
    } else {
      await Api.post(`/admin/words`, payload);
    }
    closeModal();
    loadWords();
  } catch (err) {
    errorBox.textContent = err.message;
    errorBox.style.display = "block";
  } finally {
    saveBtn.disabled = false;
    saveBtn.textContent = "Lưu từ vựng";
  }
}

function escapeAttr(str) {
  if (str === null || str === undefined) return "";
  return String(str).replaceAll('"', "&quot;");
}
