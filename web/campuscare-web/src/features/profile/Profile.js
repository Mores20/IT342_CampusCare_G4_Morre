import React, { useEffect, useState } from "react";
import Navbar from "../../shared/components/Navbar";
import { apiRequest } from "../../shared/services/api";
import "../../shared/styles/Dashboard.css";
import "./Profile.css";

const PasswordStrength = ({ password }) => {
  if (!password) return null;

  let score = 0;
  if (password.length >= 8) score++;
  if (/[A-Z]/.test(password)) score++;
  if (/[a-z]/.test(password)) score++;
  if (/[0-9]/.test(password)) score++;
  if (/[^A-Za-z0-9]/.test(password)) score++;

  const levels = [
    { label: "Weak", color: "#ef4444", width: "25%" },
    { label: "Fair", color: "#f97316", width: "50%" },
    { label: "Good", color: "#eab308", width: "75%" },
    { label: "Strong", color: "#22c55e", width: "100%" },
  ];
  const level = levels[Math.min(Math.floor(score / 1.4), 3)];

  return (
    <div className="strength-wrapper">
      <div className="strength-bar-bg">
        <div className="strength-bar-fill" style={{ width: level.width, background: level.color }} />
      </div>
      <span className="strength-label" style={{ color: level.color }}>{level.label}</span>
    </div>
  );
};

const Profile = () => {
  const userRole = localStorage.getItem("userRole");
  const [profile, setProfile] = useState({ firstName: "", lastName: "", email: "", role: "" });
  const [passwordForm, setPasswordForm] = useState({ currentPassword: "", newPassword: "", confirmPassword: "" });
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [changingPassword, setChangingPassword] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");
  const [activeTab, setActiveTab] = useState("info");

  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const data = await apiRequest("/profile");
        setProfile({
          firstName: data.firstName || "",
          lastName: data.lastName || "",
          email: data.email || "",
          role: data.role || userRole
        });
        localStorage.setItem("userFirstName", data.firstName || "");
        localStorage.setItem("userLastName", data.lastName || "");
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };
    fetchProfile();
  }, [userRole]);

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setMessage(""); setError("");
    try {
      await apiRequest("/profile", {
        method: "PUT",
        body: JSON.stringify({ firstName: profile.firstName, lastName: profile.lastName })
      });
      localStorage.setItem("userFirstName", profile.firstName);
      localStorage.setItem("userLastName", profile.lastName);
      setMessage("Profile updated successfully!");
    } catch (err) {
      setError(err.message);
    } finally {
      setSaving(false);
    }
  };

  const handlePasswordSubmit = async (e) => {
    e.preventDefault();
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setError("Passwords do not match.");
      return;
    }
    setChangingPassword(true);
    setMessage(""); setError("");
    try {
      await apiRequest("/profile/change-password", {
        method: "PUT",
        body: JSON.stringify(passwordForm)
      });
      setPasswordForm({ currentPassword: "", newPassword: "", confirmPassword: "" });
      setMessage("Password changed successfully!");
    } catch (err) {
      setError(err.message);
    } finally {
      setChangingPassword(false);
    }
  };

  const initials = `${profile.firstName.charAt(0)}${profile.lastName.charAt(0)}`.toUpperCase();

  return (
    <div className="page-wrapper">
      <Navbar role={userRole} />
      <div className="dashboard-content">

        {loading ? (
          <div className="loading">Loading profile...</div>
        ) : (
          <div className="profile-layout">

            {/* ── Left: Avatar Card ── */}
            <div className="profile-avatar-card">
              <div className="avatar-circle">{initials || "?"}</div>
              <h3 className="avatar-name">{profile.firstName} {profile.lastName}</h3>
              <p className="avatar-email">{profile.email}</p>
              <span className={`role-badge ${profile.role?.toLowerCase()}`}>
                {profile.role}
              </span>

              <div className="profile-stat-row">
                <div className="profile-stat">
                  <span className="stat-icon">📅</span>
                  <span className="stat-label">Appointments</span>
                </div>
                <div className="profile-stat">
                  <span className="stat-icon">✅</span>
                  <span className="stat-label">Completed</span>
                </div>
              </div>
            </div>

            {/* ── Right: Form Card ── */}
            <div className="profile-form-card">

              {/* Tabs */}
              <div className="profile-tabs">
                <button
                  className={`profile-tab ${activeTab === "info" ? "active" : ""}`}
                  onClick={() => { setActiveTab("info"); setMessage(""); setError(""); }}
                >
                  👤 Personal Info
                </button>
                <button
                  className={`profile-tab ${activeTab === "security" ? "active" : ""}`}
                  onClick={() => { setActiveTab("security"); setMessage(""); setError(""); }}
                >
                  🔒 Security
                </button>
              </div>

              {/* Alerts */}
              {message && <div className="profile-alert success">{message}</div>}
              {error && <div className="profile-alert error">{error}</div>}

              {/* Personal Info Tab */}
              {activeTab === "info" && (
                <form onSubmit={handleSave} className="profile-form">
                  <div className="form-row-2">
                    <div className="form-field">
                      <label>First Name</label>
                      <input
                        type="text"
                        name="firstName"
                        value={profile.firstName}
                        onChange={e => setProfile({ ...profile, firstName: e.target.value })}
                        required
                      />
                    </div>
                    <div className="form-field">
                      <label>Last Name</label>
                      <input
                        type="text"
                        name="lastName"
                        value={profile.lastName}
                        onChange={e => setProfile({ ...profile, lastName: e.target.value })}
                        required
                      />
                    </div>
                  </div>

                  <div className="form-field">
                    <label>Email Address</label>
                    <input type="email" value={profile.email} disabled className="disabled-field" />
                  </div>

                  <div className="form-field">
                    <label>Role</label>
                    <input type="text" value={profile.role} disabled className="disabled-field" />
                  </div>

                  <button type="submit" className="btn-primary" disabled={saving}>
                    {saving ? "Saving..." : "Save Changes"}
                  </button>
                </form>
              )}

              {/* Security Tab */}
              {activeTab === "security" && (
                <form onSubmit={handlePasswordSubmit} className="profile-form">
                  <div className="form-field">
                    <label>Current Password</label>
                    <input
                      type="password"
                      name="currentPassword"
                      value={passwordForm.currentPassword}
                      onChange={e => setPasswordForm({ ...passwordForm, currentPassword: e.target.value })}
                      placeholder="Enter current password"
                      required
                    />
                  </div>

                  <div className="form-field">
                    <label>New Password</label>
                    <input
                      type="password"
                      name="newPassword"
                      value={passwordForm.newPassword}
                      onChange={e => setPasswordForm({ ...passwordForm, newPassword: e.target.value })}
                      placeholder="Enter new password"
                      required
                    />
                    <PasswordStrength password={passwordForm.newPassword} />
                  </div>

                  <div className="form-field">
                    <label>Confirm New Password</label>
                    <input
                      type="password"
                      name="confirmPassword"
                      value={passwordForm.confirmPassword}
                      onChange={e => setPasswordForm({ ...passwordForm, confirmPassword: e.target.value })}
                      placeholder="Confirm new password"
                      required
                    />
                    {passwordForm.confirmPassword && passwordForm.newPassword !== passwordForm.confirmPassword && (
                      <span className="field-error">Passwords do not match</span>
                    )}
                  </div>

                  <button type="submit" className="btn-primary" disabled={changingPassword}>
                    {changingPassword ? "Updating..." : "Change Password"}
                  </button>
                </form>
              )}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Profile;