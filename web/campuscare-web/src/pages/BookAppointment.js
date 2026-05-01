import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from './Navbar';
import './Dashboard.css';
import { apiRequest, uploadFile } from '../services/api';

const BookAppointment = () => {
  const navigate = useNavigate();
  const userRole = localStorage.getItem('userRole') || 'STUDENT';

  const [formData, setFormData] = useState({
    reason: '',
    appointmentDate: '',
    appointmentTime: '',
    notes: ''   // ✅ added missing notes field
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);     // ✅ file state
  const [fileError, setFileError] = useState('');             // ✅ file error

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setError('');
  };

  const validate = () => {
    if (!formData.reason.trim()) return 'Reason is required.';
    if (!formData.appointmentDate) return 'Please select a date.';
    if (!formData.appointmentTime) return 'Please select a time.';

    const selected = new Date(`${formData.appointmentDate}T${formData.appointmentTime}`);
    if (selected <= new Date()) return 'Please select a future date and time.';

    return null;
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    setFileError('');

    if (!file) return;

    // Validate type
    const allowed = ['image/jpeg', 'image/png', 'image/gif', 'application/pdf'];
    if (!allowed.includes(file.type)) {
      setFileError('Only JPG, PNG, GIF, or PDF files are allowed.');
      setSelectedFile(null);
      return;
    }

    // Validate size — max 5MB
    if (file.size > 5 * 1024 * 1024) {
      setFileError('File must be under 5MB.');
      setSelectedFile(null);
      return;
    }

    setSelectedFile(file);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const validationError = validate();
    if (validationError) {
      setError(validationError);
      return;
    }

    setLoading(true);
    setError('');

    try {
      // Step 1 — Book appointment
      const appointment = await apiRequest('/appointments', {
        method: 'POST',
        body: JSON.stringify(formData)
      });

      // Step 2 — Upload file if selected
      if (selectedFile && appointment?.id) {
        try {
          await uploadFile(selectedFile, appointment.id);
        } catch (uploadErr) {
          // Appointment saved but file failed — show warning not error
          setSuccess('Appointment booked! Note: file upload failed — ' + uploadErr.message);
          setTimeout(() => navigate('/dashboard'), 3000);
          return;
        }
      }

      setSuccess('Appointment booked successfully! Redirecting...');
      setFormData({ reason: '', appointmentDate: '', appointmentTime: '', notes: '' });
      setSelectedFile(null);

      setTimeout(() => navigate('/dashboard'), 2000);

    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const today = new Date().toISOString().split('T')[0];

  return (
    <div className="page-wrapper">
      <Navbar role={userRole} />

      <div className="dashboard-content">
        <div className="welcome-banner">
          <h2>Book an Appointment</h2>
          <p>Fill in the details below to schedule your clinic visit</p>
        </div>

        <div className="content-card form-card">
          <h3>Appointment Details</h3>

          {error && <div className="error-alert">{error}</div>}
          {success && <div className="success-alert">{success}</div>}

          <form onSubmit={handleSubmit}>
            <div className="input-group">

              <label>Reason for Visit *</label>
              <input
                type="text"
                name="reason"
                placeholder="e.g. Fever, Check-up, Consultation"
                value={formData.reason}
                onChange={handleChange}
                required
              />

              <label>Preferred Date *</label>
              <input
                type="date"
                name="appointmentDate"
                value={formData.appointmentDate}
                min={today}
                onChange={handleChange}
                required
              />

              <label>Preferred Time *</label>
              <input
                type="time"
                name="appointmentTime"
                value={formData.appointmentTime}
                onChange={handleChange}
                required
              />

              <label>Additional Notes (optional)</label>
              <textarea
                name="notes"
                placeholder="Any additional information for the clinic..."
                value={formData.notes}
                onChange={handleChange}
              />

              {/* ✅ File Upload */}
              <label>Attach Document (optional)</label>
              <div className="file-upload-box">
                <input
                  type="file"
                  id="fileUpload"
                  accept=".jpg,.jpeg,.png,.gif,.pdf"
                  onChange={handleFileChange}
                  style={{ display: 'none' }}
                />
                <label htmlFor="fileUpload" className="file-upload-label">
                  📎 {selectedFile ? selectedFile.name : 'Choose a file (JPG, PNG, PDF — max 5MB)'}
                </label>
                {selectedFile && (
                  <button
                    type="button"
                    className="file-remove-btn"
                    onClick={() => setSelectedFile(null)}
                  >
                    ✕ Remove
                  </button>
                )}
              </div>
              {fileError && <span className="error-message">{fileError}</span>}

            </div>

            <div className="form-actions">
              <button
                type="button"
                className="btn-secondary"
                onClick={() => navigate('/dashboard')}
              >
                Cancel
              </button>
              <button
                type="submit"
                className="btn-primary"
                disabled={loading}
              >
                {loading ? 'Submitting...' : 'Submit Appointment'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};

export default BookAppointment;