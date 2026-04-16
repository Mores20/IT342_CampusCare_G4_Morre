import React from 'react';
import { useNavigate } from 'react-router-dom';
import './Dashboard.css';

const Navbar = ({ role }) => {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.clear();
    navigate('/login');
  };

  return (
    <nav className="main-nav">
      <div className="logo">
        <span className="logo-text">CampusCare</span>
      </div>

      <div className="nav-links">
        {role === 'STUDENT' && (
          <>
            <span onClick={() => navigate('/dashboard')}>Dashboard</span>
            <span onClick={() => navigate('/book')}>Book Appointment</span>
          </>
        )}
        {role === 'ADMIN' && (
          <span onClick={() => navigate('/admin')}>Admin Panel</span>
        )}
      </div>

      <button className="logout-btn" onClick={handleLogout}>Log Out</button>
    </nav>
  );
};

export default Navbar;