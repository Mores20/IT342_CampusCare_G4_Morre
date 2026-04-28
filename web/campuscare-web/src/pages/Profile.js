import React, { useEffect, useState } from "react";
import Navbar from "./Navbar";
import { apiRequest } from "../services/api";
import "./Dashboard.css";

const PasswordStrength = ({ password }) => {
  const getStrength = () => {
    let score = 0;

    if (password.length >= 8) score++;
    if (/[A-Z]/.test(password)) score++;
    if (/[a-z]/.test(password)) score++;
    if (/[0-9]/.test(password)) score++;
    if (/[^A-Za-z0-9]/.test(password)) score++;

    if (score <= 2)
      return {
        text: "Weak",
        width: "33%"
      };

    if (score <= 4)
      return {
        text: "Medium",
        width: "66%"
      };

    return {
      text: "Strong",
      width: "100%"
    };
  };

  const strength = getStrength();

  if (!password) return null;

  return (
    <div style={{ marginTop: "10px" }}>
      <div
        style={{
          height: "8px",
          background: "#ddd",
          borderRadius: "10px",
          overflow: "hidden"
        }}
      >
        <div
          style={{
            height: "100%",
            width: strength.width,
            background: "linear-gradient(to right,#ff4d4d,#ffc107,#28a745)",
            transition: "0.3s"
          }}
        />
      </div>

      <small
        style={{
          display: "block",
          marginTop: "6px",
          fontWeight: "600"
        }}
      >
        Password Strength: {strength.text}
      </small>
    </div>
  );
};

const Profile = () => {
  const userRole = localStorage.getItem("userRole");

  const [profile, setProfile] = useState({
    firstName: "",
    lastName: "",
    email: "",
    role: ""
  });

  const [passwordForm, setPasswordForm] = useState({
    currentPassword: "",
    newPassword: "",
    confirmPassword: ""
  });

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [changingPassword, setChangingPassword] =
    useState(false);

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const data = await apiRequest("/profile");

        setProfile({
          firstName: data.firstName || "",
          lastName: data.lastName || "",
          email: data.email || "",
          role: data.role?.name || userRole
        });

        localStorage.setItem(
          "userFirstName",
          data.firstName || ""
        );
        localStorage.setItem(
          "userLastName",
          data.lastName || ""
        );
        localStorage.setItem(
          "userEmail",
          data.email || ""
        );
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, [userRole]);

  const handleChange = (e) => {
    setProfile({
      ...profile,
      [e.target.name]: e.target.value
    });
  };

  const handlePasswordChange = (e) => {
    setPasswordForm({
      ...passwordForm,
      [e.target.name]: e.target.value
    });
  };

  const handleSave = async (e) => {
    e.preventDefault();

    setSaving(true);
    setMessage("");
    setError("");

    try {
      await apiRequest("/profile", {
        method: "PUT",
        body: JSON.stringify({
          firstName: profile.firstName,
          lastName: profile.lastName
        })
      });

      localStorage.setItem(
        "userFirstName",
        profile.firstName
      );
      localStorage.setItem(
        "userLastName",
        profile.lastName
      );

      setMessage("Profile updated successfully.");
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();

    if (
      passwordForm.newPassword !==
      passwordForm.confirmPassword
    ) {
      setError("Passwords do not match.");
      return;
    }

    setChangingPassword(true);
    setMessage("");
    setError("");

    try {
      await apiRequest("/profile/change-password", {
        method: "PUT",
        body: JSON.stringify(passwordForm)
      });

      setPasswordForm({
        currentPassword: "",
        newPassword: "",
        confirmPassword: ""
      });

      setMessage("Password changed successfully.");
    } catch (err) {
      setError(err.message);
    } finally {
      setChangingPassword(false);
    }
  };

  return (
    <div className="page-wrapper">
      <Navbar role={userRole} />

      <div className="dashboard-content">
        <div className="welcome-banner">
          <h2>My Profile 👤</h2>
          <p>
            Manage your personal information and
            security
          </p>
        </div>

        {loading ? (
          <div className="loading">
            Loading profile...
          </div>
        ) : (
          <div
            style={{
              display: "grid",
              gap: "25px",
              maxWidth: "800px",
              margin: "0 auto"
            }}
          >
            {message && (
              <div
                style={{
                  background: "#d4edda",
                  color: "#155724",
                  padding: "12px",
                  borderRadius: "10px"
                }}
              >
                {message}
              </div>
            )}

            {error && (
              <div
                style={{
                  background: "#f8d7da",
                  color: "#721c24",
                  padding: "12px",
                  borderRadius: "10px"
                }}
              >
                {error}
              </div>
            )}

            {/* Profile Card */}
            <div className="content-card">
              <div
                style={{
                  textAlign: "center",
                  marginBottom: "20px"
                }}
              >
                <div
                  style={{
                    width: "90px",
                    height: "90px",
                    borderRadius: "50%",
                    background: "#0d6efd",
                    color: "white",
                    fontSize: "36px",
                    display: "flex",
                    justifyContent: "center",
                    alignItems: "center",
                    margin: "0 auto 15px"
                  }}
                >
                  {profile.firstName.charAt(0)}
                </div>

                <h3>
                  {profile.firstName}{" "}
                  {profile.lastName}
                </h3>
                <p>{profile.email}</p>
              </div>

              <form onSubmit={handleSave}>
                <div
                  style={{
                    display: "grid",
                    gridTemplateColumns:
                      "1fr 1fr",
                    gap: "15px"
                  }}
                >
                  <div>
                    <label>First Name</label>
                    <input
                      type="text"
                      name="firstName"
                      value={profile.firstName}
                      onChange={handleChange}
                      className="form-control"
                      required
                    />
                  </div>

                  <div>
                    <label>Last Name</label>
                    <input
                      type="text"
                      name="lastName"
                      value={profile.lastName}
                      onChange={handleChange}
                      className="form-control"
                      required
                    />
                  </div>
                </div>

                <div
                  style={{
                    marginTop: "15px"
                  }}
                >
                  <label>Email</label>
                  <input
                    type="email"
                    value={profile.email}
                    disabled
                    className="form-control"
                  />
                </div>

                <div
                  style={{
                    marginTop: "15px"
                  }}
                >
                  <label>Role</label>
                  <input
                    type="text"
                    value={profile.role}
                    disabled
                    className="form-control"
                  />
                </div>

                <button
                  type="submit"
                  className="btn-primary"
                  disabled={saving}
                  style={{
                    marginTop: "20px"
                  }}
                >
                  {saving
                    ? "Saving..."
                    : "Save Changes"}
                </button>
              </form>
            </div>

            {/* Password Card */}
            <div className="content-card">
              <h3>Security 🔒</h3>
              <p>
                Change your account password
              </p>

              <form
                onSubmit={
                  handlePasswordSubmit
                }
              >
                <div
                  style={{
                    marginTop: "15px"
                  }}
                >
                  <label>
                    Current Password
                  </label>
                  <input
                    type="password"
                    name="currentPassword"
                    value={
                      passwordForm.currentPassword
                    }
                    onChange={
                      handlePasswordChange
                    }
                    className="form-control"
                    required
                  />
                </div>

                <div
                  style={{
                    marginTop: "15px"
                  }}
                >
                  <label>
                    New Password
                  </label>
                  <input
                    type="password"
                    name="newPassword"
                    value={
                      passwordForm.newPassword
                    }
                    onChange={
                      handlePasswordChange
                    }
                    className="form-control"
                    required
                  />
                  <PasswordStrength
                    password={
                      passwordForm.newPassword
                    }
                  />
                </div>

                <div
                  style={{
                    marginTop: "15px"
                  }}
                >
                  <label>
                    Confirm Password
                  </label>
                  <input
                    type="password"
                    name="confirmPassword"
                    value={
                      passwordForm.confirmPassword
                    }
                    onChange={
                      handlePasswordChange
                    }
                    className="form-control"
                    required
                  />
                </div>

                <button
                  type="submit"
                  className="btn-primary"
                  disabled={
                    changingPassword
                  }
                  style={{
                    marginTop: "20px"
                  }}
                >
                  {changingPassword
                    ? "Updating..."
                    : "Change Password"}
                </button>
              </form>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Profile;