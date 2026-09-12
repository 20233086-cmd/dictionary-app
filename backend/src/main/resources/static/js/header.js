/**
 * Render phần tài khoản bên phải thanh điều hướng (Đăng nhập/Đăng ký hoặc tên user + đăng xuất)
 * và đánh dấu link đang active. Gọi renderHeader() sau khi DOM đã tải.
 */
function renderHeader(activePage) {
  const authSlot = document.getElementById("nav-auth-slot");
  const adminLink = document.getElementById("nav-admin-link");
  if (!authSlot) return;

  const user = Api.getUser();

  if (user) {
    authSlot.innerHTML = `
      <div class="nav-user">
        <span class="nav-user__name">${escapeHtml(user.fullName)}</span>
        <button class="btn btn--ghost btn--sm" id="btn-logout" type="button">Đăng xuất</button>
      </div>
    `;
    document.getElementById("btn-logout").addEventListener("click", () => Api.logout());

    if (adminLink) {
      adminLink.style.display = user.role === "ADMIN" ? "inline-flex" : "none";
    }
    let adminAiLink = document.getElementById("nav-admin-ai-link");
    if (user.role === "ADMIN" && !adminAiLink) {
      const navLinks = document.querySelector(".nav__links");
      if (navLinks) {
        navLinks.insertAdjacentHTML("beforeend", '<a href="admin-ai.html" id="nav-admin-ai-link" data-nav-link="admin-ai" class="nav__link">AI Admin</a>');
      }
    }
    adminAiLink = document.getElementById("nav-admin-ai-link");
    if (adminAiLink) adminAiLink.style.display = user.role === "ADMIN" ? "inline-flex" : "none";
  } else {
    authSlot.innerHTML = `
      <a class="btn btn--ghost btn--sm" href="login.html">Đăng nhập</a>
      <a class="btn btn--primary btn--sm" href="register.html">Đăng ký</a>
    `;
    if (adminLink) adminLink.style.display = "none";
  }

  document.querySelectorAll("[data-nav-link]").forEach((link) => {
      link.classList.remove("nav__link--active");
      if (link.getAttribute("data-nav-link") === activePage) {
        link.classList.add("nav__link--active");
      }
  });
}

function escapeHtml(str) {
  if (str === null || str === undefined) return "";
  return String(str)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#039;");
}

function requireLogin(redirectTo) {
  if (!Api.isLoggedIn()) {
    window.location.href = "login.html?redirect=" + encodeURIComponent(redirectTo || "index.html");
    return false;
  }
  return true;
}
