document.addEventListener('DOMContentLoaded', async () => {
  renderHeader('dashboard');
  if (!requireLogin('dashboard.html')) return;
  const cards = document.getElementById('stat-cards');
  
  try {
    const d = await Api.get('/dashboard');
    
    // Danh sách thẻ thống kê cá nhân mặc định
    let cardItems = [
      ['📖','Từ điển', d.totalWords, 'tổng số từ'],
      ['⭐','Yêu thích', d.favorites, 'từ đã lưu'],
      ['🧠','Flashcard', d.flashcards, 'thẻ đang học'],
      ['⏰','Đến hạn', d.dueToday, 'thẻ cần ôn hôm nay'],
      ['🔎','Lịch sử', d.history, 'lượt tra gần đây']
    ];

    // Kiểm tra nếu là Admin thì gọi thêm API lấy thống kê Admin
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    if (user && user.roles && user.roles.includes('ROLE_ADMIN')) {
      try {
        const adminData = await Api.get('/dashboard/admin/stats');
        if (adminData) {
          cardItems.unshift(
            ['👥', 'Tổng User', adminData.totalUsers, 'người dùng hệ thống'],
            ['📊', 'Lượt tra', adminData.totalSearches, 'tổng lượt tìm kiếm']
          );
        }
      } catch (adminErr) {
        console.error('Không thể tải dữ liệu Admin Stats:', adminErr);
      }
    }

    // Render danh sách các thẻ ra giao diện
    cards.innerHTML = cardItems.map(x => 
      `<div class="card stat-card"><span class="stat-card__icon">${x[0]}</span><div><strong>${x[2]}</strong><small>${x[1]} · ${x[3]}</small></div></div>`
    ).join('');

    document.getElementById('due-task').textContent = d.dueToday + ' thẻ';
    const w = d.wordOfDay;
    const m = w.meanings?.[0];
    document.getElementById('wotd').innerHTML = `<div class="wotd-large__word">${escapeHtml(w.english)}</div><div class="wotd-large__meta">${escapeHtml(w.phonetic || '')} ${w.level ? `<span class="badge badge--level">${escapeHtml(w.level)}</span>` : ''}</div><p>${escapeHtml(m?.vietnamese || '')}</p><p class="muted">${escapeHtml(m?.exampleEn || '')}</p><a class="btn btn--ghost btn--sm" href="index.html?word=${encodeURIComponent(w.english)}">Tra từ này</a>`;
    document.getElementById('speak-word').onclick = () => speakWord(w.english);
  } catch (e) { 
    cards.innerHTML = `<div class="alert alert--error" style="display:block">${escapeHtml(e.message)}</div>`; 
  }
});

function speakWord(t){ 
  if(!('speechSynthesis' in window)) return; 
  const u=new SpeechSynthesisUtterance(t);
  u.lang='en-US';
  speechSynthesis.cancel();
  speechSynthesis.speak(u); 
}
