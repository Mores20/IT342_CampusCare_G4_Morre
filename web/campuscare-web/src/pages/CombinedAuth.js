import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { GoogleLogin } from '@react-oauth/google';
import './Auth.css';

const CombinedAuth = () => {
  const navigate = useNavigate();
  const [activeTab, setActiveTab] = useState('login');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  const [loginData, setLoginData] = useState({ email: '', password: '' });

  const [registerData, setRegisterData] = useState({
    firstName: '', lastName: '', email: '', password: '', confirmPassword: ''
  });
  const [errors, setErrors] = useState({});

  // ── Handlers ──
  const handleLoginChange = (e) => {
    setLoginData({ ...loginData, [e.target.name]: e.target.value });
    setError('');
  };

  const handleRegisterChange = (e) => {
    setRegisterData({ ...registerData, [e.target.name]: e.target.value });
    if (errors[e.target.name]) setErrors({ ...errors, [e.target.name]: '' });
  };

  const validateRegister = () => {
    const e = {};
    if (!registerData.firstName.trim()) e.firstName = 'Required';
    if (!registerData.lastName.trim())  e.lastName  = 'Required';
    if (!registerData.email.trim())     e.email     = 'Required';
    else if (!/\S+@\S+\.\S+/.test(registerData.email)) e.email = 'Invalid email';
    if (!registerData.password)          e.password  = 'Required';
    else if (registerData.password.length < 6) e.password = 'Min. 6 characters';
    if (registerData.password !== registerData.confirmPassword) e.confirmPassword = 'Passwords do not match';
    return e;
  };

  const saveAuth = (data, fallbackEmail = '') => {
    localStorage.setItem('isAuthenticated', 'true');
    localStorage.setItem('authToken',    data.accessToken  || '');
    localStorage.setItem('refreshToken', data.refreshToken || '');
    localStorage.setItem('userEmail',    data.email        || fallbackEmail);
    localStorage.setItem('userFirstName',data.firstName    || '');
    localStorage.setItem('userLastName', data.lastName     || '');
    localStorage.setItem('userRole',     data.role         || 'STUDENT');
  };

  // ── Login ──
  const handleLoginSubmit = async (e) => {
    e.preventDefault();
    setLoading(true); setError('');
    try {
      const res  = await fetch('http://localhost:8080/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(loginData)
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.message || 'Login failed');
      saveAuth(data, loginData.email);
      navigate(data.role === 'ADMIN' ? '/admin' : '/dashboard');
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  // ── Register ──
  const handleRegisterSubmit = async (e) => {
    e.preventDefault();
    const errs = validateRegister();
    if (Object.keys(errs).length) { setErrors(errs); return; }

    setLoading(true); setErrors({});
    try {
      const res  = await fetch('http://localhost:8080/auth/register', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          firstName: registerData.firstName,
          lastName:  registerData.lastName,
          email:     registerData.email,
          password:  registerData.password
        })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.message || 'Registration failed');

      setSuccessMessage('Account created! Redirecting to login...');
      setRegisterData({ firstName: '', lastName: '', email: '', password: '', confirmPassword: '' });
      setTimeout(() => { setActiveTab('login'); setSuccessMessage(''); }, 2000);
    } catch (err) {
      setErrors({ submit: err.message });
    } finally {
      setLoading(false);
    }
  };

  // ── Google ──
  const handleGoogleSuccess = async (credentialResponse) => {
    setLoading(true); setError('');
    try {
      const res  = await fetch('http://localhost:8080/auth/google', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token: credentialResponse.credential })
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data.message || 'Google login failed');
      saveAuth(data);
      navigate(data.role === 'ADMIN' ? '/admin' : '/dashboard');
    } catch (err) {
      setError(err.message || 'Google login failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-page">

      {/* ── Left: Branding ── */}
      <div className="auth-branding">
        <p className="brand-label">Welcome to</p>
        <h1 className="brand-name">CampusCare</h1>
        <p className="brand-tagline">
          Fast. Simple. <span>Secure Appointments</span>
        </p>
      </div>

      {/* ── Right: Form Panel ── */}
      <div className="auth-panel">
        <div className="auth-card">

          {/* Tabs */}
          <div className="auth-tabs">
            <button
              className={`auth-tab ${activeTab === 'login' ? 'active' : ''}`}
              onClick={() => { setActiveTab('login'); setError(''); setErrors({}); }}
            >
              Login
            </button>
            <button
              className={`auth-tab ${activeTab === 'register' ? 'active' : ''}`}
              onClick={() => { setActiveTab('register'); setError(''); setErrors({}); }}
            >
              Register
            </button>
          </div>

          <div className="auth-body">

            {/* ── Login Form ── */}
            {activeTab === 'login' && (
              <form onSubmit={handleLoginSubmit}>
                {error && <div className="auth-alert error">{error}</div>}

                <div className="auth-input-wrap">
                  <input
                    className="auth-input"
                    type="email"
                    name="email"
                    placeholder="Email"
                    value={loginData.email}
                    onChange={handleLoginChange}
                    required
                  />
                </div>

                <div className="auth-input-wrap">
                  <input
                    className="auth-input"
                    type="password"
                    name="password"
                    placeholder="Password"
                    value={loginData.password}
                    onChange={handleLoginChange}
                    required
                  />
                </div>

                {/* Login button — left aligned like Figma */}
                <div className="btn-row-left">
                  <button className="auth-submit" type="submit" disabled={loading}>
                    {loading ? 'Signing in...' : 'Login'}
                  </button>
                </div>

                <div className="auth-divider">or</div>

                <div className="google-wrap">
                  <GoogleLogin
                    onSuccess={handleGoogleSuccess}
                    onError={() => setError('Google login failed')}
                    theme="outline"
                    size="large"
                    width="100%"
                    text="signin_with"
                    shape="rectangular"
                  />
                </div>
              </form>
            )}

            {/* ── Register Form ── */}
            {activeTab === 'register' && (
              <form onSubmit={handleRegisterSubmit}>
                {errors.submit   && <div className="auth-alert error">{errors.submit}</div>}
                {successMessage  && <div className="auth-alert success">{successMessage}</div>}

                <div className="auth-input-wrap">
                  <input
                    className={`auth-input ${errors.firstName ? 'err' : ''}`}
                    type="text"
                    name="firstName"
                    placeholder="First Name"
                    value={registerData.firstName}
                    onChange={handleRegisterChange}
                    required
                  />
                  {errors.firstName && <span className="field-err">{errors.firstName}</span>}
                </div>

                <div className="auth-input-wrap">
                  <input
                    className={`auth-input ${errors.lastName ? 'err' : ''}`}
                    type="text"
                    name="lastName"
                    placeholder="Last Name"
                    value={registerData.lastName}
                    onChange={handleRegisterChange}
                    required
                  />
                  {errors.lastName && <span className="field-err">{errors.lastName}</span>}
                </div>

                <div className="auth-input-wrap">
                  <input
                    className={`auth-input ${errors.email ? 'err' : ''}`}
                    type="email"
                    name="email"
                    placeholder="Email"
                    value={registerData.email}
                    onChange={handleRegisterChange}
                    required
                  />
                  {errors.email && <span className="field-err">{errors.email}</span>}
                </div>

                <div className="auth-input-wrap">
                  <input
                    className={`auth-input ${errors.password ? 'err' : ''}`}
                    type="password"
                    name="password"
                    placeholder="Password (min. 6 characters)"
                    value={registerData.password}
                    onChange={handleRegisterChange}
                    required
                  />
                  {errors.password && <span className="field-err">{errors.password}</span>}
                </div>

                <div className="auth-input-wrap">
                  <input
                    className={`auth-input ${errors.confirmPassword ? 'err' : ''}`}
                    type="password"
                    name="confirmPassword"
                    placeholder="Confirm Password"
                    value={registerData.confirmPassword}
                    onChange={handleRegisterChange}
                    required
                  />
                  {errors.confirmPassword && <span className="field-err">{errors.confirmPassword}</span>}
                </div>

                {/* Register button — right aligned like Figma */}
                <div className="btn-row-right">
                  <button className="auth-submit" type="submit" disabled={loading}>
                    {loading ? 'Creating...' : 'Register'}
                  </button>
                </div>
              </form>
            )}

          </div>
        </div>
      </div>
    </div>
  );
};

export default CombinedAuth;