let currentConversationId = null;
let currentChatMode = "QA";

document.addEventListener("DOMContentLoaded", () => {
  const fab = document.getElementById("chat-fab");
  const panel = document.getElementById("chat-panel");
  const closeBtn = document.getElementById("chat-close");
  const sendBtn = document.getElementById("chat-send");
  const input = document.getElementById("chat-input");

  if (!fab || !panel) return;

  fab.addEventListener("click", () => {
    panel.classList.toggle("is-open");
    if (panel.classList.contains("is-open")) initChatBody();
  });

  closeBtn.addEventListener("click", () => panel.classList.remove("is-open"));

  sendBtn.addEventListener("click", sendChatMessage);
  input.addEventListener("keydown", (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      sendChatMessage();
    }
  });
  input.addEventListener("input", () => {
    input.style.height = "auto";
    input.style.height = Math.min(input.scrollHeight, 80) + "px";
  });
});

function initChatBody() {
  const body = document.getElementById("chat-body");
  const footer = document.getElementById("chat-footer");
  const header = document.querySelector(".chat-panel__header");

  if (!Api.isLoggedIn()) {
    footer.style.display = "none";
    removeModeToggle();
    body.innerHTML = `
      <div class="chat-login-hint">
        Đăng nhập để trò chuyện cùng Dictly Bot — trợ lý AI giúp giải thích từ vựng,
        ngữ pháp và dịch câu nhanh chóng.<br><br>
        <a href="login.html">Đăng nhập ngay</a>
      </div>
    `;
    return;
  }

  footer.style.display = "flex";

  if (!document.getElementById("chat-mode-toggle")) {
    const toggle = document.createElement("div");
    toggle.className = "chat-mode-toggle";
    toggle.id = "chat-mode-toggle";
    toggle.innerHTML = `
      <button type="button" class="chat-mode-toggle__btn is-active" data-mode="QA">💬 Hỏi đáp</button>
      <button type="button" class="chat-mode-toggle__btn" data-mode="PRACTICE">🗣️ Luyện hội thoại</button>
    `;
    header.insertAdjacentElement("afterend", toggle);

    toggle.querySelectorAll("[data-mode]").forEach((btn) => {
      btn.addEventListener("click", () => {
        const mode = btn.getAttribute("data-mode");
        if (mode === currentChatMode) return;
        currentChatMode = mode;
        currentConversationId = null; // đổi chế độ -> bắt đầu cuộc trò chuyện mới
        toggle.querySelectorAll("[data-mode]").forEach((b) => b.classList.remove("is-active"));
        btn.classList.add("is-active");
        resetChatBody(mode);
      });
    });
  }

  if (!body.dataset.initialized) {
    resetChatBody(currentChatMode);
    body.dataset.initialized = "true";
  }
}

function removeModeToggle() {
  const toggle = document.getElementById("chat-mode-toggle");
  if (toggle) toggle.remove();
}

function resetChatBody(mode) {
  const body = document.getElementById("chat-body");
  const welcome = mode === "PRACTICE"
    ? "Hi there! 👋 Let's practice English together. Tell me about your day, and I'll gently correct any mistakes as we chat."
    : "Xin chào! Mình là Dictly Bot 👋 Hỏi mình về nghĩa từ vựng, cách dùng, ngữ pháp, hoặc nhờ mình dịch một câu bất kỳ nhé.";
  body.innerHTML = "";
  appendBotBubble(body, welcome);
}

async function sendChatMessage() {
  const input = document.getElementById("chat-input");
  const body = document.getElementById("chat-body");
  const sendBtn = document.getElementById("chat-send");

  const message = input.value.trim();
  if (!message) return;

  appendUserBubble(body, message);
  input.value = "";
  input.style.height = "auto";
  sendBtn.disabled = true;

  const typingEl = document.createElement("div");
  typingEl.className = "chat-msg chat-msg--bot chat-msg--typing";
  typingEl.textContent = "Dictly Bot đang soạn câu trả lời...";
  body.appendChild(typingEl);
  body.scrollTop = body.scrollHeight;

  try {
    const res = await Api.post("/chatbot/message", {
      conversationId: currentConversationId,
      mode: currentChatMode,
      message,
    });
    currentConversationId = res.conversationId;
    currentChatMode = res.mode || currentChatMode;
    typingEl.remove();
    appendBotBubble(body, res.reply);
  } catch (err) {
    typingEl.remove();
    appendBotBubble(body, "Xin lỗi, có lỗi xảy ra: " + err.message);
  } finally {
    sendBtn.disabled = false;
  }
}

function appendUserBubble(body, text) {
  const div = document.createElement("div");
  div.className = "chat-msg chat-msg--user";
  div.textContent = text;
  body.appendChild(div);
  body.scrollTop = body.scrollHeight;
}

/**
 * Bot messages có thể chứa cú pháp [[word]] do backend chèn vào cho các từ đã có
 * trong từ điển — render thành link bấm để tra từ ngay (mở index.html?word=...).
 */
function appendBotBubble(body, text) {
  const div = document.createElement("div");
  div.className = "chat-msg chat-msg--bot";

  const parts = String(text).split(/(\[\[[A-Za-z']+\]\])/g);
  parts.forEach((part) => {
    const match = part.match(/^\[\[([A-Za-z']+)\]\]$/);
    if (match) {
      const a = document.createElement("a");
      a.href = "index.html?word=" + encodeURIComponent(match[1]);
      a.className = "chat-word-link";
      a.textContent = match[1];
      a.title = "Bấm để tra từ này";
      div.appendChild(a);
    } else if (part) {
      div.appendChild(document.createTextNode(part));
    }
  });

  body.appendChild(div);
  body.scrollTop = body.scrollHeight;
}

/**
 * Bot messages render cú pháp Markdown và chuyển cú pháp [[word]] thành link tra từ.
 */
function appendBotBubble(body, text) {
  const div = document.createElement("div");
  div.className = "chat-msg chat-msg--bot";

  // 1. Thay thế cú pháp [[word]] thành thẻ HTML liên kết trước khi parse Markdown
  let processedText = String(text).replace(
    /\[\[([A-Za-z']+)\]\]/g,
    (match, word) => {
      return `<a href="index.html?word=${encodeURIComponent(word)}" class="chat-word-link" title="Bấm để tra từ này">${word}</a>`;
    }
  );

  // 2. Chuyển đổi Markdown sang HTML (Nếu có thư viện marked.js)
  if (typeof marked !== "undefined") {
    div.innerHTML = marked.parse(processedText);
  } else {
    // Trường hợp chưa load marked, dùng innerHTML đơn giản để giữ được thẻ <a> tra từ
    div.innerHTML = processedText;
  }

  body.appendChild(div);
  body.scrollTop = body.scrollHeight;
}
