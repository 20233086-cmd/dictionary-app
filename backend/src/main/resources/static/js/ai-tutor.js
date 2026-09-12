document.addEventListener('DOMContentLoaded', () => {
  renderHeader('');
  if (!requireLogin('ai.html')) return;

  const messages = document.getElementById('tutor-messages');
  const input = document.getElementById('tutor-input');
  const send = document.getElementById('btn-tutor-send');

  // Hàm chuyển đổi Markdown sang HTML bằng Marked.js
  function formatTutor(text) {
    if (!text) return '';
    if (typeof marked !== 'undefined') {
      return marked.parse(text);
    }
    // Fallback cơ bản nếu chưa load kịp thư viện
    return escapeHtml(text)
      .replace(/\n/g, '<br>')
      .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
  }

  function addMessage(text, type = 'bot') {
    const el = document.createElement('div');
    el.className = `tutor-message tutor-message--${type}`;
    
    // Tạo cấu trúc thẻ đúng khớp với CSS đã định nghĩa
    el.innerHTML = `
      <div class="tutor-avatar">${type === 'bot' ? '✦' : 'U'}</div>
      <div class="tutor-message__content">
        <strong>${type === 'bot' ? 'AI Tutor' : 'Bạn'}</strong>
        <div class="tutor-message__body">${formatTutor(text)}</div>
      </div>
    `;
    
    messages.appendChild(el);
    messages.scrollTop = messages.scrollHeight;
  }

  async function ask(text) {
    text = text.trim(); 
    if (!text) return;

    addMessage(text, 'user'); 
    input.value = ''; 
    send.disabled = true; 
    send.textContent = 'Đang trả lời...';

    const loading = document.createElement('div'); 
    loading.className = 'tutor-typing'; 
    loading.textContent = 'AI Tutor đang suy nghĩ...'; 
    messages.appendChild(loading); 
    messages.scrollTop = messages.scrollHeight;

    try { 
      const reply = await Api.post('/ai/tutor/chat', { message: text }); 
      loading.remove(); 
      addMessage(reply, 'bot'); 
    } catch(e) { 
      loading.remove(); 
      addMessage('Không thể kết nối AI: ' + e.message, 'bot'); 
    } finally { 
      send.disabled = false; 
      send.textContent = 'Gửi ✦'; 
      input.focus(); 
    }
  }

  send.onclick = () => ask(input.value);
  
  input.addEventListener('keydown', e => { 
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      ask(input.value);
    } 
  });

  document.querySelectorAll('[data-prompt]').forEach(b => b.onclick = () => {
    input.value = b.dataset.prompt;
    ask(input.value);
  });

  document.querySelectorAll('.ai-tool-btn').forEach(b => b.onclick = () => {
    const action = b.dataset.action;
    const prompts = {
      explain: 'Giải thích một từ vựng tiếng Anh theo format: nghĩa tiếng Việt, từ loại, IPA, 2 ví dụ, từ đồng nghĩa và trái nghĩa. Hãy chọn một từ trình độ B1.',
      translate: 'Hãy hướng dẫn tôi dịch câu tiếng Anh sang tiếng Việt theo ngữ cảnh và cho một ví dụ.',
      practice: 'Hãy bắt đầu một cuộc hội thoại tiếng Anh trình độ B1. Hỏi tôi một câu trước và sửa lỗi ngắn gọn sau mỗi câu trả lời.'
    };
    input.value = prompts[action] || '';
    ask(input.value);
  });

  document.getElementById('btn-clear-chat').onclick = () => {
    messages.innerHTML = `
      <div class="tutor-message tutor-message--bot">
        <div class="tutor-avatar">✦</div>
        <div class="tutor-message__content">
          <strong>AI Tutor</strong>
          <div class="tutor-message__body">Đã bắt đầu phiên học mới. Bạn muốn học gì?</div>
        </div>
      </div>`;
  };

  document.getElementById('btn-correct').onclick = async () => {
    const sentence = document.getElementById('correct-input').value.trim();
    const out = document.getElementById('correct-result');
    const btn = document.getElementById('btn-correct');
    
    if (!sentence) {
      out.innerHTML = '<span class="muted">Hãy nhập một câu trước.</span>';
      return;
    }

    btn.disabled = true;
    btn.textContent = 'Đang kiểm tra...';
    out.innerHTML = '';

    try { 
      const text = await Api.post('/ai/tutor/correct', { message: sentence }); 
      out.innerHTML = `<div class="tutor-message__body">${formatTutor(text)}</div>`; 
    } catch(e) {
      out.innerHTML = `<div class="alert alert--error" style="display:block">${escapeHtml(e.message)}</div>`;
    } finally {
      btn.disabled = false;
      btn.textContent = 'Kiểm tra câu';
    }
  };
});
