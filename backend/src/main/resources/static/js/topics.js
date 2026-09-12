function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;');
}

function escapeAttr(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}
document.addEventListener('DOMContentLoaded', async () => {
  renderHeader('topics');
  const grid = document.getElementById('topic-grid');
  try {
    const topics = await Api.get('/words/topics');
    const icons = {'Technology':'💻','Education':'🎓','Travel':'✈️','Work & Career':'💼','Environment':'🌱','Communication':'💬','Personal Development':'🚀','Daily English':'☀️'};
    grid.innerHTML = topics.length ? topics.map(t => `<button class="topic-card" data-topic="${escapeAttr(t)}"><span class="topic-card__icon">${icons[t] || '📚'}</span><strong>${escapeHtml(t)}</strong><small>Khám phá từ vựng →</small></button>`).join('') : `<div class="card empty-state"><p class="empty-state__title">Chưa có chủ đề</p></div>`;
    grid.querySelectorAll('[data-topic]').forEach(btn => btn.addEventListener('click', () => loadTopic(btn.dataset.topic)));
  } catch(e) { grid.innerHTML=`<div class="alert alert--error" style="display:block">${escapeHtml(e.message)}</div>`; }
});
async function loadTopic(topic){
  const el=document.getElementById('topic-result'); el.innerHTML=`<div class="card loading-inline">Đang tải từ vựng...</div>`;
  try{ const page=await Api.get('/words/topic/'+encodeURIComponent(topic)+'?size=50'); el.innerHTML=`<div class="card"><div class="section-heading"><div><p class="side-card__label">CHỦ ĐỀ</p><h2>${escapeHtml(topic)}</h2></div><span class="badge">${page.totalElements} từ</span></div><div class="vocab-grid">${page.content.map(w=>`<a class="vocab-card" href="index.html?word=${encodeURIComponent(w.english)}"><div><strong>${escapeHtml(w.english)}</strong>${w.phonetic?`<small>${escapeHtml(w.phonetic)}</small>`:''}</div><span>${escapeHtml(w.meanings?.[0]?.vietnamese || '')}</span>${w.level?`<em>${escapeHtml(w.level)}</em>`:''}</a>`).join('')}</div></div>`; window.scrollTo({top:document.body.scrollHeight,behavior:'smooth'}); }catch(e){el.innerHTML=`<div class="alert alert--error" style="display:block">${escapeHtml(e.message)}</div>`;}
}
