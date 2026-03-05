import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
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
      
      // Store login state (you might want to store a token if your backend returns one)
      localStorage.setItem('isAuthenticated', 'true');
      localStorage.setItem('userEmail', formData.email);
      
      // Show success message
      alert('Login successful!');
      
      // Redirect to dashboard or home page
      navigate('/dashboard'); // Change this to your desired route
      
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
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
        </div>
      </div>
    </div>
  );
};

export default Login;