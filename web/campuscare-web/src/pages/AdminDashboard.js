import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from './Navbar';
import './Dashboard.css';
import { apiRequest } from '../services/api';

const AdminDashboard = () => {
  const navigate = useNavigate();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filterStatus, setFilterStatus] = useState('ALL');

   useEffect(() => {
    const userRole = localStorage.getItem('userRole');
    const authToken = localStorage.getItem('authToken');
    
    if (!authToken) {
      navigate('/login');
      return;
    }
    
    if (userRole !== 'ADMIN') {
      navigate('/dashboard');
      return;
    }
    
    fetchAllAppointments();
  }, [navigate]);

  useEffect(() => {
    fetchAllAppointments();
  }, []);

  const fetchAllAppointments = async () => {
    try {
      const data = await apiRequest("/appointments/all");
      setAppointments(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };


  const updateStatus = async (id, newStatus) => {
    try {
      await apiRequest(`/appointments/${id}/status`, {
        method: "PUT",
        body: JSON.stringify({ status: newStatus })
      });

      setAppointments(prev =>
        prev.map(a => a.id === id ? { ...a, status: newStatus } : a)
      );
    } catch (err) {
      alert("Error: " + err.message);
    }
  };

  const filtered = filterStatus === 'ALL'
    ? appointments
    : appointments.filter(a => a.status === filterStatus);

  return (
    <div className="page-wrapper">
      <Navbar role="ADMIN" />

      <div className="dashboard-content">
        <div className="welcome-banner">
          <h2>Admin Panel 🏥</h2>
          <p>Manage all student appointments</p>
        </div>

        {/* Filter Tabs */}
        <div className="filter-tabs">
          {['ALL', 'PENDING', 'APPROVED', 'COMPLETED', 'CANCELLED'].map(status => (
            <button
              key={status}
              className={`filter-tab ${filterStatus === status ? 'active' : ''}`}
              onClick={() => setFilterStatus(status)}
            >
              {status}
            </button>
          ))}
        </div>

        {error && <div className="error-alert">{error}</div>}

        {loading ? (
          <div className="loading">Loading appointments...</div>
        ) : (
          <div className="admin-container">
            {filtered.length === 0 ? (
              <p style={{ color: 'white', textAlign: 'center', padding: '20px' }}>
                No appointments found.
              </p>
            ) : (
              filtered.map(item => (
                <div className="admin-card" key={item.id}>
            
                  <div className="admin-info">
                    {/* Update this line to match the nested user object from Spring Boot */}
                    <h4>{item.user?.firstName} {item.user?.lastName}</h4>
                    <p>{item.user?.email}</p>
                    <p><strong>Reason:</strong> {item.reason}</p>
                    <p style={{ fontSize: '12px', color: '#666' }}>
                      {item.appointmentDate} at {item.appointmentTime}
                    </p>
                  </div>

                  <div className="admin-actions">
                    <span className={`status-badge ${item.status.toLowerCase()}`}>
                      {item.status}
                    </span>

                    {item.status === 'PENDING' && (
                      <>
                        <button
                          className="btn approve"
                          onClick={() => updateStatus(item.id, 'APPROVED')}
                        >
                          Approve
                        </button>
                        <button
                          className="btn reject"
                          onClick={() => updateStatus(item.id, 'CANCELLED')}
                        >
                          Cancel
                        </button>
                      </>
                    )}

                    {item.status === 'APPROVED' && (
                      <button
                        className="btn approve"
                        onClick={() => updateStatus(item.id, 'COMPLETED')}
                      >
                        Mark Complete
                      </button>
                    )}
                  </div>

                </div>
              ))
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default AdminDashboard;