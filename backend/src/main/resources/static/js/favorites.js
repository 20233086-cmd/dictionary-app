document.addEventListener("DOMContentLoaded", () => {
  renderHeader("favorites");
  if (!requireLogin("favorites.html")) return;
  loadFavorites();
});

async function loadFavorites() {
  const container = document.getElementById("favorites-container");
  try {
    const words = await Api.get("/favorites");
    if (!words.length) {
      container.innerHTML = `
        <div class="empty-state">
          <svg class="empty-state__icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
          </svg>
          <p class="empty-state__title">Chưa có từ yêu thích nào</p>
          <p>Hãy tra một từ ở trang chủ và bấm biểu tượng trái tim để lưu lại.</p>
        </div>
      `;
      return;
    }

    container.innerHTML = `<div class="row-list">${words.map((w) => `
      <div class="row-item" data-id="${w.id}">
        <div class="row-item__main">
          <a href="index.html?word=${encodeURIComponent(w.english)}" class="row-item__word">${escapeHtml(w.english)}</a>
          <span class="row-item__meaning">${escapeHtml((w.meanings[0] && w.meanings[0].vietnamese) || "")}</span>
        </div>
        <button class="btn btn--danger btn--sm" data-remove="${w.id}">Bỏ thích</button>
      </div>
    `).join("")}</div>`;

    container.querySelectorAll("[data-remove]").forEach((btn) => {
      btn.addEventListener("click", async () => {
        const id = btn.getAttribute("data-remove");
        try {
          await Api.del(`/favorites/${id}`);
          btn.closest(".row-item").remove();
          if (!container.querySelector(".row-item")) loadFavorites();
        } catch (err) {
          alert(err.message);
        }
      });
    });
  } catch (err) {
    container.innerHTML = `<div class="alert alert--error" style="display:block;">${escapeHtml(err.message)}</div>`;
  }
}
