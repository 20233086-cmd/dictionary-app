/**
 * Lớp tiện ích gọi API backend, tự động gắn JWT token và parse ApiResponse chuẩn.
 */
const Api = {
  getToken() {
    return localStorage.getItem("dictly_token");
  },

  setSession(authData) {
    localStorage.setItem("dictly_token", authData.token);
    localStorage.setItem("dictly_user", JSON.stringify({
      userId: authData.userId,
      fullName: authData.fullName,
      username: authData.username,
      role: authData.role,
    }));
  },

  getUser() {
    const raw = localStorage.getItem("dictly_user");
    return raw ? JSON.parse(raw) : null;
  },

  isLoggedIn() {
    return !!this.getToken();
  },

  isAdmin() {
    const user = this.getUser();
    return !!user && user.role === "ADMIN";
  },

  logout() {
    localStorage.removeItem("dictly_token");
    localStorage.removeItem("dictly_user");
    window.location.href = "index.html";
  },

  async request(path, options = {}) {
    const headers = { "Content-Type": "application/json", ...(options.headers || {}) };
    const token = this.getToken();
    if (token) headers["Authorization"] = "Bearer " + token;

    let res;
    try {
      res = await fetch(API_BASE_URL + path, { ...options, headers });
    } catch (networkErr) {
      throw new Error("Không thể kết nối tới máy chủ. Hãy kiểm tra backend đã chạy ở " + API_BASE_URL + " chưa.");
    }

    let body;
    try {
      body = await res.json();
    } catch (parseErr) {
      throw new Error("Phản hồi từ máy chủ không hợp lệ (mã " + res.status + ")");
    }

    if (!res.ok || body.success === false) {
      throw new Error(body.message || "Đã có lỗi xảy ra");
    }
    return body.data;
  },

  get(path) {
    return this.request(path, { method: "GET" });
  },
  post(path, data) {
    return this.request(path, { method: "POST", body: data !== undefined ? JSON.stringify(data) : undefined });
  },
  put(path, data) {
    return this.request(path, { method: "PUT", body: JSON.stringify(data) });
  },
  del(path) {
    return this.request(path, { method: "DELETE" });
  },
};
