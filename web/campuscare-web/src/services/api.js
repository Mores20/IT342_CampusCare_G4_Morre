const BASE_URL = "http://localhost:8080";

// ── Core request function with auto-refresh ──
export const apiRequest = async (endpoint, options = {}) => {
  const token = localStorage.getItem("authToken");

  const response = await fetch(`${BASE_URL}${endpoint}`, {
    ...options,
    headers: {
      "Content-Type": "application/json",
      ...(token ? { Authorization: `Bearer ${token}` } : {}),
      ...(options.headers || {}),
    },
  });

  // ✅ Try to refresh token on 401
  if (response.status === 401) {
    const refreshed = await tryRefreshToken();
    if (refreshed) {
      // Retry original request with new token
      const newToken = localStorage.getItem("authToken");
      const retryResponse = await fetch(`${BASE_URL}${endpoint}`, {
        ...options,
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${newToken}`,
          ...(options.headers || {}),
        },
      });

      if (!retryResponse.ok) {
        const err = await retryResponse.json().catch(() => ({}));
        throw new Error(err.message || "Request failed");
      }

      return handleResponse(retryResponse);
    } else {
      // Refresh failed — force logout
      localStorage.clear();
      window.location.href = "/login";
      return;
    }
  }

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || "Request failed");
  }

  return handleResponse(response);
};

// ── Handle response (JSON or text) ──
const handleResponse = async (response) => {
  const contentType = response.headers.get("content-type");
  if (contentType && contentType.includes("application/json")) {
    return response.json();
  }
  return response.text();
};

// ── Try to get a new access token using refresh token ──
const tryRefreshToken = async () => {
  const refreshToken = localStorage.getItem("refreshToken");
  if (!refreshToken) return false;

  try {
    const response = await fetch(`${BASE_URL}/auth/refresh`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
    });

    if (!response.ok) return false;

    const data = await response.json();
    localStorage.setItem("authToken", data.accessToken);
    return true;
  } catch {
    return false;
  }
};

// ── File upload (separate — uses multipart/form-data) ──
export const uploadFile = async (file, appointmentId = null) => {
  const token = localStorage.getItem("authToken");

  const formData = new FormData();
  formData.append("file", file);
  if (appointmentId) {
    formData.append("appointmentId", appointmentId);
  }

  const response = await fetch(`${BASE_URL}/files/upload`, {
    method: "POST",
    headers: {
      Authorization: `Bearer ${token}`,
      // ⚠️ Do NOT set Content-Type here — browser sets it with boundary automatically
    },
    body: formData,
  });

  if (!response.ok) {
    const err = await response.json().catch(() => ({}));
    throw new Error(err.message || "Upload failed");
  }

  return response.json();
};

// ── Logout (clears refresh token on backend too) ──
export const logout = async () => {
  const refreshToken = localStorage.getItem("refreshToken");

  try {
    await fetch(`${BASE_URL}/auth/logout`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ refreshToken }),
    });
  } catch {
    // Ignore errors — clear local storage regardless
  } finally {
    localStorage.clear();
    window.location.href = "/login";
  }
};