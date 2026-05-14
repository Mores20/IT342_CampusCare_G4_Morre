import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from "../../shared/components/Navbar";
import "../../shared/styles/Dashboard.css";
import { apiRequest } from "../../shared/services/api";

const BASE_URL = 'http://localhost:8080';

const AdminDashboard = () => {
  const navigate = useNavigate();
  const [appointments, setAppointments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [files, setFiles] = useState({});
  const [expandedId, setExpandedId] = useState(null);

  const token = localStorage.getItem('authToken');

  useEffect(() => {
    const userRole = localStorage.getItem('userRole');
    const authToken = localStorage.getItem('authToken');

    if (!authToken) { navigate('/login'); return; }
    if (userRole !== 'ADMIN') { navigate('/dashboard'); return; }

    fetchAllAppointments();
  }, [navigate]);

  const fetchAllAppointments = async () => {
    try {
      const data = await apiRequest('/appointments/all');
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
        method: 'PUT',
        body: JSON.stringify({ status: newStatus })
      });
      setAppointments(prev =>
        prev.map(a => a.id === id ? { ...a, status: newStatus } : a)
      );
    } catch (err) {
      alert('Error: ' + err.message);
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
              <p style={{ color: '#555', textAlign: 'center', padding: '20px' }}>
                No appointments found.
              </p>
            ) : (
              filtered.map(item => (
                <div key={item.id}>
                  <div className="admin-card">

                    {/* Student Info */}
                    <div className="admin-info">
                      <h4>{item.user?.firstName} {item.user?.lastName}</h4>
                      <p>{item.user?.email}</p>
                      <p><strong>Reason:</strong> {item.reason}</p>
                      <p style={{ fontSize: '12px', color: '#666' }}>
                        {item.appointmentDate} at {item.appointmentTime}
                      </p>

                      {/* ✅ View Files button per appointment */}
                      <button
                        className="btn-outline"
                        style={{ marginTop: '8px', fontSize: '12px', padding: '5px 12px' }}
                        onClick={() => fetchFiles(item.id)}
                      >
                        📎 {expandedId === item.id ? 'Hide Files' : 'View Student Files'}
                      </button>
                    </div>

                    {/* Status + Actions */}
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

                  {/* ✅ Expandable file list below each card */}
                  {expandedId === item.id && (
                    <div className="file-list admin-file-list">
                      {!files[item.id] ? (
                        <p className="file-empty">Loading files...</p>
                      ) : files[item.id].length === 0 ? (
                        <p className="file-empty">No files uploaded by student.</p>
                      ) : (
                        files[item.id].map(f => (
                          <div className="file-item" key={f.id}>
                            <span className="file-name">📄 {f.fileName}</span>
                            <div className="file-actions">
                              <button
                                className="file-btn view"
                                onClick={() => handleView(f.id)}
                              >
                                View
                              </button>
                              <button
                                className="file-btn download"
                                onClick={() => handleDownload(f.id, f.fileName)}
                              >
                                Download
                              </button>
                            </div>
                          </div>
                        ))
                      )}
                    </div>
                  )}

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