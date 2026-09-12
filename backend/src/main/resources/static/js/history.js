document.addEventListener("DOMContentLoaded", () => {
  renderHeader("history");
  if (!requireLogin("history.html")) return;
  loadHistory();

  document.getElementById("btn-clear-history").addEventListener("click", async () => {
    if (!confirm("Bạn có chắc muốn xoá toàn bộ lịch sử tra cứu?")) return;
    try {
      await Api.del("/history");
      loadHistory();
    } catch (err) {
      alert(err.message);
    }
  });
});

async function loadHistory() {
  const container = document.getElementById("history-container");
  try {
    const keywords = await Api.get("/history?limit=30");
    if (!keywords.length) {
      container.innerHTML = `
        <div class="empty-state">
          <svg class="empty-state__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <circle cx="12" cy="12" r="10"></circle>
            <polyline points="12 6 12 12 16 14"></polyline>
          </svg>
          <p class="empty-state__title">Chưa có lịch sử</p>
          <p>Các từ bạn tra cứu sẽ xuất hiện ở đây.</p>
        </div>
      `;
      return;
    }

    container.innerHTML = keywords.map((k) => `
      <a href="index.html?word=${encodeURIComponent(k)}" class="history-chip">${escapeHtml(k)}</a>
    `).join("");
  } catch (err) {
    container.innerHTML = `<div class="alert alert--error" style="display:block;">${escapeHtml(err.message)}</div>`;
  }
}
