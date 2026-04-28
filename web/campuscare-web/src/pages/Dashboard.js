  import React, { useState, useEffect } from 'react';
  import { useNavigate } from 'react-router-dom';
  import Navbar from './Navbar';
  import './Dashboard.css';
  import { apiRequest } from '../services/api';

  const Dashboard = () => {
    const navigate = useNavigate(); 

    const [appointments, setAppointments] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    
    const firstName = localStorage.getItem("userFirstName") || "Student";    

    useEffect(() => {
      fetchAppointments();
    }, []);

  const fetchAppointments = async () => {
    try {
      const data = await apiRequest("/appointments/my");
      setAppointments(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

    const upcoming = appointments.find(
      a => a.status === 'PENDING' || a.status === 'APPROVED'
    );
    const history = appointments.filter(a =>
      a.status === 'COMPLETED' || a.status === 'CANCELLED'
    );

    return (
      <div className="page-wrapper">
        <Navbar role="STUDENT" />

        <div className="dashboard-content">

          {/* Welcome Banner */}
          <div className="welcome-banner">
            <h2>Welcome, {firstName} 👋</h2>
            <p>Here's your appointment overview</p>
          </div>

          {error && <div className="error-alert">{error}</div>}

          {loading ? (
            <div className="loading">Loading your appointments...</div>
          ) : (
            <div className="dashboard-grid">

              {/* Upcoming Appointment */}
              <div className="content-card highlight-card">
                <h3>Upcoming Appointment</h3>
                {upcoming ? (
                  <div className="upcoming-box">
                    <p><strong>Reason:</strong> {upcoming.reason}</p>
                    <p><strong>Date:</strong> {upcoming.appointmentDate}</p>
                    <p><strong>Time:</strong> {upcoming.appointmentTime}</p>
                    <span className={`status-badge ${upcoming.status.toLowerCase()}`}>
                      {upcoming.status}
                    </span>
                  </div>
                ) : (
                  <div className="empty-state">
                    <p>No upcoming appointments.</p>
                    <button
                      className="btn-primary"
                      onClick={() => navigate('/book')}
                    >
                      Book Now
                    </button>
                  </div>
                )}
              </div>

              {/* Appointment History */}
              <div className="content-card">
                <h3>Appointment History</h3>
                {history.length === 0 ? (
                  <p>No past appointments yet.</p>
                ) : (
                  <table className="history-table">
                    <thead>
                      <tr>
                        <th>Reason</th>
                        <th>Date</th>
                        <th>Time</th>
                        <th>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {history.map(h => (
                        <tr key={h.id}>
                          <td>{h.reason}</td>
                          <td>{h.appointmentDate}</td>
                          <td>{h.appointmentTime}</td>
                          <td>
                            <span className={`status-badge ${h.status.toLowerCase()}`}>
                              {h.status}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>

            </div>
          )}

          {/* Book Button */}
          <div style={{ marginTop: '20px', textAlign: 'right' }}>
            <button className="btn-primary" onClick={() => navigate('/book')}>
              + Book New Appointment
            </button>
          </div>

        </div>
      </div>
    );
  };

  export default Dashboard;