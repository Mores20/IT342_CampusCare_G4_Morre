import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from './Navbar';
import './Dashboard.css';
import { apiRequest } from '../services/api';

const BookAppointment = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    reason: '',
    appointmentDate: '',
    appointmentTime: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setError('');
  };

  const validate = () => {
    if (!formData.reason.trim()) return 'Reason is required.';
    if (!formData.appointmentDate) return 'Please select a date.';
    if (!formData.appointmentTime) return 'Please select a time.';

    // Must be a future date
    const selected = new Date(`${formData.appointmentDate}T${formData.appointmentTime}`);
    if (selected <= new Date()) return 'Please select a future date and time.';

    return null;
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
      await apiRequest("/appointments", {
        method: "POST",
        body: JSON.stringify(formData)
      });

      setSuccess("Appointment booked successfully!");
      setFormData({
        reason: '',
        appointmentDate: '',
        appointmentTime: ''
      });

      setTimeout(() => navigate('/dashboard'), 2000);

    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  // Get today's date as min value for date input
  const today = new Date().toISOString().split('T')[0];

  return (
    <div className="page-wrapper">
      <Navbar role="STUDENT" />

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