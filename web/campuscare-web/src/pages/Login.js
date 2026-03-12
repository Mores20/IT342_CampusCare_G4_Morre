import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { GoogleLogin } from '@react-oauth/google';
import './Auth.css';

const Login = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: '',
    password: ''
  });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleInputChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
    setError('');
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await fetch('http://localhost:8080/auth/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email: formData.email,
          password: formData.password
        })
      });

      const data = await response.text();

      if (!response.ok) {
        throw new Error(data || 'Login failed');
      }

      // Login successful
      console.log('Login response:', data);
      
      // Store login state
      localStorage.setItem('isAuthenticated', 'true');
      localStorage.setItem('userEmail', formData.email);
      
      alert('Login successful!');
      navigate('/dashboard');
      
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleSuccess = async (credentialResponse) => {
    try {
      setLoading(true);
      setError('');
      
      console.log('Google credential received:', credentialResponse);
      
      // Send the Google token to your backend
      const response = await fetch('http://localhost:8080/auth/google', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          token: credentialResponse.credential
        })
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || 'Google login failed');
      }

      // Login successful
      console.log('Google login response:', data);
      
      // Store login state
      localStorage.setItem('isAuthenticated', 'true');
      localStorage.setItem('authToken', data.accessToken || data.token);
      if (data.email) localStorage.setItem('userEmail', data.email);
      
      alert('Google login successful!');
      navigate('/dashboard');
      
    } catch (err) {
      setError(err.message || 'Google login failed');
      console.error('Google login error:', err);
    } finally {
      setLoading(false);
    }
  };

  const handleGoogleError = () => {
    setError('Google login failed. Please try again.');
    console.log('Google login failed');
  };

  return (
    <div className="auth-container">
      <div className="auth-card">
        <div className="welcome-section">
          <h1 className="welcome-title">Welcome to</h1>
          <h1 className="app-name">CampusCare</h1>
          <p className="tagline">Fast. Simple. Secure Appointments</p>
        </div>

        <div className="form-section">
          <div className="tabs">
            <button 
              className="tab active"
              onClick={() => navigate('/login')}
            >
              Login
            </button>
            <button 
              className="tab"
              onClick={() => navigate('/register')}
            >
              Register
            </button>
          </div>

          <form onSubmit={handleSubmit}>
            {error && <div className="error-alert">{error}</div>}
            
            <div className="input-group">
              <input
                type="email"
                name="email"
                placeholder="Email"
                value={formData.email}
                onChange={handleInputChange}
                required
              />
            </div>

            <div className="input-group">
              <input
                type="password"
                name="password"
                placeholder="Password"
                value={formData.password}
                onChange={handleInputChange}
                required
              />
            </div>

            <button 
              type="submit" 
              className="submit-btn"
              disabled={loading}
            >
              {loading ? 'Logging in...' : 'Login'}
            </button>
          </form>

          <div className="divider">
            <span>or</span>
          </div>

          <div className="google-login-wrapper">
            <GoogleLogin
              onSuccess={handleGoogleSuccess}
              onError={handleGoogleError}
              theme="outline"
              size="large"
              width="100%"
              text="signin_with"
              shape="rectangular"
            />
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;