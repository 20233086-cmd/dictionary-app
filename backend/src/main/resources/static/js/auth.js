document.addEventListener("DOMContentLoaded", () => {
  renderHeader("");

  const loginForm = document.getElementById("login-form");
  if (loginForm) {
    loginForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const errorBox = document.getElementById("login-error");
      const btn = document.getElementById("btn-submit");
      errorBox.style.display = "none";
      btn.disabled = true;
      btn.textContent = "Đang đăng nhập...";

      try {
        const data = await Api.post("/auth/login", {
          username: document.getElementById("username").value.trim(),
          password: document.getElementById("password").value,
        });
        Api.setSession(data);
        const params = new URLSearchParams(window.location.search);
        window.location.href = params.get("redirect") || "index.html";
      } catch (err) {
        errorBox.textContent = err.message;
        errorBox.style.display = "block";
        btn.disabled = false;
        btn.textContent = "Đăng nhập";
      }
    });
  }

  const registerForm = document.getElementById("register-form");
  if (registerForm) {
    registerForm.addEventListener("submit", async (e) => {
      e.preventDefault();
      const errorBox = document.getElementById("register-error");
      const btn = document.getElementById("btn-submit");
      errorBox.style.display = "none";
      btn.disabled = true;
      btn.textContent = "Đang tạo tài khoản...";

      try {
        const data = await Api.post("/auth/register", {
          fullName: document.getElementById("fullName").value.trim(),
          username: document.getElementById("username").value.trim(),
          email: document.getElementById("email").value.trim(),
          password: document.getElementById("password").value,
        });
        Api.setSession(data);
        window.location.href = "index.html";
      } catch (err) {
        errorBox.textContent = err.message;
        errorBox.style.display = "block";
        btn.disabled = false;
        btn.textContent = "Đăng ký";
      }
    });
  }
});
