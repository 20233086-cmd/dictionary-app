document.addEventListener('DOMContentLoaded', () => {
  renderHeader('admin-ai');

  if (!Api.isLoggedIn()) {
    location.href = 'login.html?redirect=admin-ai.html';
    return;
  }
  if (!Api.isAdmin()) {
    alert('Bạn không có quyền truy cập AI Admin.');
    location.href = 'index.html';
    return;
  }

  loadOverview();

  const input = document.getElementById('admin-input');
  const send = document.getElementById('btn-admin-send');
  const messages = document.getElementById('admin-messages');

  function scrollToBottom() {
    requestAnimationFrame(() => {
      messages.scrollTop = messages.scrollHeight;
    });
  }

  function format(text) {
    if (typeof marked !== 'undefined') {
		marked.setOptions({ breaks: true });
      return marked.parse(String(text));
    }
    return `<p>${escapeHtml(text).replace(/\n/g, '<br>')}</p>`;
  }

  function add(text, type) {
    const el = document.createElement('div');
    el.className = `tutor-message tutor-message--${type}`;

    const isBot = type === 'bot';
    const avatar = isBot ? 'AI' : 'A';
    const title = isBot ? 'Admin Assistant' : 'Admin';

    el.innerHTML = `
      <div class="tutor-avatar">${avatar}</div>
      <div class="tutor-message__content">
        <strong>${title}</strong>
        <div class="tutor-message__body">${isBot ? format(text) : `<p>${escapeHtml(text)}</p>`}</div>
      </div>
    `;

    messages.appendChild(el);
    scrollToBottom();
  }

  async function ask(text) {
    text = text.trim();
    if (!text) return;

    add(text, 'user');
    input.value = '';
    send.disabled = true;
    send.textContent = 'Đang phân tích...';

    const loading = document.createElement('div');
    loading.className = 'tutor-typing';
    loading.textContent = 'AI đang đọc dữ liệu hệ thống...';
    messages.appendChild(loading);
    scrollToBottom();

    try {
      const reply = await Api.post('/admin/ai/chat', { message: text });
      loading.remove();
      add(reply, 'bot');
    } catch (e) {
      loading.remove();
      add('Lỗi: ' + e.message, 'bot');
    } finally {
      send.disabled = false;
      send.textContent = 'Phân tích ✦';
      input.focus();
    }
  }

  send.onclick = () => ask(input.value);

  input.addEventListener('keydown', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      ask(input.value);
    }
  });

  document.querySelectorAll('[data-admin-prompt]').forEach((b) => {
    b.onclick = () => {
      input.value = b.dataset.adminPrompt;
      ask(input.value);
    };
  });

  document.getElementById('btn-refresh-overview').onclick = loadOverview;
});

async function loadOverview() {
  const grid = document.getElementById('admin-stat-grid');
  const top = document.getElementById('admin-topwords');

  try {
    const d = await Api.get('/admin/ai/overview');
    grid.innerHTML = `
      <div class="admin-stat"><span>👤</span><strong>${d.totalUsers}</strong><small>Người dùng</small></div>
      <div class="admin-stat"><span>📖</span><strong>${d.totalWords}</strong><small>Từ vựng</small></div>
      <div class="admin-stat"><span>🔎</span><strong>${d.totalSearches}</strong><small>Lượt tra</small></div>
      <div class="admin-stat"><span>☀️</span><strong>${d.todaySearches}</strong><small>Lượt tra hôm nay</small></div>
      <div class="admin-stat"><span>⭐</span><strong>${d.totalFavorites}</strong><small>Lượt yêu thích</small></div>
      <div class="admin-stat"><span>🤖</span><strong>${d.totalConversations}</strong><small>Hội thoại AI</small></div>
    `;

    top.innerHTML = `
      <p class="side-card__label">TOP TỪ ĐƯỢC TRA</p>
      <div class="top-word-list">
        ${
          (d.topWords || [])
            .map(
              (w, i) =>
                `<div><b>${i + 1}</b><span>${escapeHtml(w.english)}</span><small>${w.viewCount || 0} lượt</small></div>`
            )
            .join('') || '<span class="muted">Chưa có dữ liệu.</span>'
        }
      </div>
    `;
  } catch (e) {
    grid.innerHTML = `<div class="alert alert--error" style="display:block">${escapeHtml(e.message)}</div>`;
  }
}
