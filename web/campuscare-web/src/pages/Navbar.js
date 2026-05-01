import React from "react";
import { useNavigate } from "react-router-dom";
import "./Dashboard.css";

const Navbar = ({ role }) => {
  const navigate = useNavigate();

  const firstName = localStorage.getItem("userFirstName") || "User";
  const lastName = localStorage.getItem("userLastName") || "";

  const handleLogout = async () => {
    const refreshToken = localStorage.getItem("refreshToken");
    try {
      await fetch("http://localhost:8080/auth/logout", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ refreshToken })
      });
    } catch (e) {
      // ignore errors — clear storage regardless
    } finally {
      localStorage.clear();
      navigate("/login");
    }
  };

  return (
    <nav className="main-nav">

      {/* Logo */}
      <div
        className="logo"
        style={{ cursor: "pointer" }}
        onClick={() => role === "ADMIN" ? navigate("/admin") : navigate("/dashboard")}
      >
        <span className="logo-text">CampusCare</span>
      </div>

      {/* Navigation Links */}
      <div className="nav-links">
        {role === "STUDENT" && (
          <>
            <span onClick={() => navigate("/dashboard")}>Dashboard</span>
            <span onClick={() => navigate("/book")}>Book Appointment</span>
            <span onClick={() => navigate("/profile")}>Profile</span>
          </>
        )}

        {role === "ADMIN" && (
          <>
            <span onClick={() => navigate("/admin")}>Admin Panel</span>
            <span onClick={() => navigate("/profile")}>Profile</span>
          </>
        )}
      </div>

      {/* User Info + Logout */}
      <div style={{ display: "flex", alignItems: "center", gap: "15px" }}>

        {/* ✅ Fixed — visible text with proper contrast */}
        <div style={{
          background: "#e8f0fe",
          color: "#0355A1",
          padding: "8px 14px",
          borderRadius: "30px",
          fontSize: "14px",
          fontWeight: "600"
        }}>
          👤 {firstName} {lastName}
        </div>

        <button className="logout-btn" onClick={handleLogout}>
          Log Out
        </button>

      </div>
    </nav>
  );
};

export default Navbar;