import React from "react";
import { useNavigate } from "react-router-dom";
import "./Dashboard.css";

const Navbar = ({ role }) => {
  const navigate = useNavigate();

  const firstName =
    localStorage.getItem("userFirstName") || "User";

  const lastName =
    localStorage.getItem("userLastName") || "";

  const handleLogout = () => {
    localStorage.clear();
    navigate("/login");
  };

  return (
    <nav className="main-nav">
      {/* Logo */}
      <div
        className="logo"
        style={{ cursor: "pointer" }}
        onClick={() =>
          role === "ADMIN"
            ? navigate("/admin")
            : navigate("/dashboard")
        }
      >
        <span className="logo-text">CampusCare</span>
      </div>

      {/* Navigation Links */}
      <div className="nav-links">
        {role === "STUDENT" && (
          <>
            <span onClick={() => navigate("/dashboard")}>
              Dashboard
            </span>

            <span onClick={() => navigate("/book")}>
              Book Appointment
            </span>

            <span onClick={() => navigate("/profile")}>
              Profile
            </span>
          </>
        )}

        {role === "ADMIN" && (
          <>
            <span onClick={() => navigate("/admin")}>
              Admin Panel
            </span>

            <span onClick={() => navigate("/profile")}>
              Profile
            </span>
          </>
        )}
      </div>

      {/* User Info + Logout */}
      <div
        style={{
          display: "flex",
          alignItems: "center",
          gap: "15px"
        }}
      >
        <div
          style={{
            background: "#ffffff22",
            padding: "8px 14px",
            borderRadius: "30px",
            fontSize: "14px",
            color: "white",
            fontWeight: "600"
          }}
        >
          👤 {firstName} {lastName}
        </div>

        <button
          className="logout-btn"
          onClick={handleLogout}
        >
          Log Out
        </button>
      </div>
    </nav>
  );
};

export default Navbar;