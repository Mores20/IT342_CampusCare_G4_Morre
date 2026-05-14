import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from "../../shared/components/Navbar";
import "../../shared/styles/Dashboard.css";
import { apiRequest } from "../../shared/services/api";

const BASE_URL = 'http://localhost:8080';

const Dashboard = () => {
  const navigate = useNavigate();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [files, setFiles] = useState({});
  const [expandedId, setExpandedId] = useState(null);

  const firstName = localStorage.getItem('userFirstName') || 'Student';
  const token = localStorage.getItem('authToken');

  useEffect(() => {
    fetchAppointments();
  }, []);

  const fetchAppointments = async () => {
    try {
      const data = await apiRequest('/appointments/my');
      setAppointments(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const fetchFiles = async (appointmentId) => {
    if (expandedId === appointmentId) {
      setExpandedId(null);
      return;
    }
    try {
      const data = await apiRequest(`/files/appointment/${appointmentId}`);
      setFiles(prev => ({ ...prev, [appointmentId]: data }));
      setExpandedId(appointmentId);
    } catch (err) {
      console.error('Failed to fetch files:', err.message);
    }
  };

  const handleView = (fileId) => {
    window.open(`${BASE_URL}/files/download/${fileId}?inline=true`, '_blank');
  };

  const handleDownload = (fileId, fileName) => {
    fetch(`${BASE_URL}/files/download/${fileId}?inline=false`, {
      headers: { Authorization: `Bearer ${token}` }
    })
      .then(res => res.blob())
      .then(blob => {
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = fileName;
        a.click();
        window.URL.revokeObjectURL(url);
      })
      .catch(err => alert('Download failed: ' + err.message));
  };

  const upcoming = appointments.find(
    a => a.status === 'PENDING' || a.status === 'APPROVED'
  );
  const history = appointments.filter(a =>
    a.status === 'COMPLETED' || a.status === 'CANCELLED'
  );

  const FileList = ({ appointmentId }) => {
    const fileList = files[appointmentId];
    if (!fileList) return null;
    return (
      <div className="file-list">
        {fileList.length === 0 ? (
          <p className="file-empty">No files attached.</p>
        ) : (
          fileList.map(f => (
            <div className="file-item" key={f.id}>
              <span className="file-name">📄 {f.fileName}</span>
              <div className="file-actions">
                <button className="file-btn view" onClick={() => handleView(f.id)}>
                  View
                </button>
                <button className="file-btn download" onClick={() => handleDownload(f.id, f.fileName)}>
                  Download
                </button>
              </div>
            </div>
          ))
        )}
      </div>
    );
  };

  return (
    <div className="page-wrapper">
      <Navbar role="STUDENT" />

      <div className="dashboard-content">

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

                  {/* ✅ File toggle for upcoming */}
                  <div style={{ marginTop: '14px' }}>
                    <button
                      className="btn-outline"
                      onClick={() => fetchFiles(upcoming.id)}
                    >
                      📎 {expandedId === upcoming.id ? 'Hide Files' : 'View Attached Files'}
                    </button>
                    {expandedId === upcoming.id && <FileList appointmentId={upcoming.id} />}
                  </div>
                </div>
              ) : (
                <div className="empty-state">
                  <p>No upcoming appointments.</p>
                  <button className="btn-primary" onClick={() => navigate('/book')}>
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
                      <th>Files</th>
                    </tr>
                  </thead>
                  <tbody>
                    {history.map(h => (
                      <React.Fragment key={h.id}>
                        <tr>
                          <td>{h.reason}</td>
                          <td>{h.appointmentDate}</td>
                          <td>{h.appointmentTime}</td>
                          <td>
                            <span className={`status-badge ${h.status.toLowerCase()}`}>
                              {h.status}
                            </span>
                          </td>
                          <td>
                            <button
                              className="file-btn view"
                              onClick={() => fetchFiles(h.id)}
                            >
                              {expandedId === h.id ? 'Hide' : '📎 Files'}
                            </button>
                          </td>
                        </tr>
                        {expandedId === h.id && (
                          <tr>
                            <td colSpan="5">
                              <FileList appointmentId={h.id} />
                            </td>
                          </tr>
                        )}
                      </React.Fragment>
                    ))}
                  </tbody>
                </table>
              )}
            </div>

          </div>
        )}

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