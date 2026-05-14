import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import Navbar from "../../shared/components/Navbar";
import "../../shared/styles/Dashboard.css";
import { apiRequest, uploadFile } from "../../shared/services/api";

// ✅ All clinic time slots
const ALL_SLOTS = [
  '08:00', '08:30', '09:00', '09:30', '10:00', '10:30',
  '11:00', '11:30', '13:00', '13:30', '14:00', '14:30',
  '15:00', '15:30', '16:00', '16:30',
];

const BookAppointment = () => {
  const navigate = useNavigate();
  const userRole = localStorage.getItem('userRole') || 'STUDENT';

  const [formData, setFormData] = useState({
    reason: '',
    appointmentDate: '',
    appointmentTime: '',
    notes: ''
  });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [selectedFile, setSelectedFile] = useState(null);
  const [fileError, setFileError] = useState('');
  const [bookedSlots, setBookedSlots] = useState([]);
  const [slotsLoading, setSlotsLoading] = useState(false);

  const now = new Date();
  const todayStr = now.toISOString().split('T')[0];
  const isToday = formData.appointmentDate === todayStr;

  const isSlotDisabled = (slot) => {
    const taken = bookedSlots.includes(slot);
    if (taken) return true;
    if (!isToday) return false;
    const [slotHour, slotMinute] = slot.split(':').map(Number);
    const currentHour   = now.getHours();
    const currentMinute = now.getMinutes();
    return slotHour < currentHour || (slotHour === currentHour && slotMinute <= currentMinute);
  };

  const availableCount = ALL_SLOTS.filter(s => !isSlotDisabled(s)).length;

  useEffect(() => {
    if (!formData.appointmentDate) {
      setBookedSlots([]);
      return;
    }
    setSlotsLoading(true);
    setFormData(prev => ({ ...prev, appointmentTime: '' }));
    apiRequest(`/appointments/booked-slots?date=${formData.appointmentDate}`)
      .then(data => setBookedSlots(data || []))
      .catch(() => setBookedSlots([]))
      .finally(() => setSlotsLoading(false));
  }, [formData.appointmentDate]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
    setError('');
  };

  const handleSlotSelect = (slot) => {
    if (isSlotDisabled(slot)) return;
    setFormData(prev => ({ ...prev, appointmentTime: slot }));
    setError('');
  };

  const validate = () => {
    if (!formData.reason.trim()) return 'Reason is required.';
    if (!formData.appointmentDate) return 'Please select a date.';
    if (!formData.appointmentTime) return 'Please select a time slot.';
    const selected = new Date(`${formData.appointmentDate}T${formData.appointmentTime}`);
    if (selected <= new Date()) return 'Please select a future date and time.';
    return null;
  };

  const handleFileChange = (e) => {
    const file = e.target.files[0];
    setFileError('');
    if (!file) return;
    const allowed = ['image/jpeg', 'image/png', 'image/gif', 'application/pdf'];
    if (!allowed.includes(file.type)) {
      setFileError('Only JPG, PNG, GIF, or PDF files are allowed.');
      setSelectedFile(null);
      return;
    }
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
    if (validationError) { setError(validationError); return; }

    setLoading(true);
    setError('');

    try {
      const appointment = await apiRequest('/appointments', {
        method: 'POST',
        body: JSON.stringify(formData)
      });

      if (selectedFile && appointment?.id) {
        try {
          await uploadFile(selectedFile, appointment.id);
        } catch (uploadErr) {
          setSuccess('Appointment booked! Note: file upload failed — ' + uploadErr.message);
          setTimeout(() => navigate('/dashboard'), 3000);
          return;
        }
      }

      setSuccess('Appointment booked successfully! Redirecting...');
      setTimeout(() => navigate('/dashboard'), 2000);

    } catch (err) {
      setError(err.message);
      if (formData.appointmentDate) {
        apiRequest(`/appointments/booked-slots?date=${formData.appointmentDate}`)
          .then(data => setBookedSlots(data || []))
          .catch(() => {});
      }
    } finally {
      setLoading(false);
    }
  };

  const today = new Date().toISOString().split('T')[0];

  const formatSlot = (slot) => {
    const [h, m] = slot.split(':');
    const hour = parseInt(h);
    const ampm = hour >= 12 ? 'PM' : 'AM';
    const display = hour > 12 ? hour - 12 : hour === 0 ? 12 : hour;
    return `${display}:${m} ${ampm}`;
  };

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
                min={todayStr}
                onChange={handleChange}
                required
              />

              {/* Date availability indicator */}
              {formData.appointmentDate && !slotsLoading && (
                <div className={`slot-availability-badge ${availableCount === 0 ? 'badge-full' : availableCount <= 4 ? 'badge-limited' : 'badge-available'}`}>
                  {availableCount === 0
                    ? '⛔ No slots available on this date'
                    : availableCount <= 4
                    ? `⚠️ Only ${availableCount} slot${availableCount === 1 ? '' : 's'} left`
                    : `✅ ${availableCount} slots available`}
                </div>
              )}
              {slotsLoading && (
                <div className="slot-availability-badge badge-loading">Checking availability...</div>
              )}

              {/* Visual time slot grid */}
              {formData.appointmentDate && !slotsLoading && (
                <>
                  <label>Select a Time Slot *</label>
                  <div className="slot-grid">
                    {ALL_SLOTS.map(slot => {
                      const disabled = isSlotDisabled(slot);
                      const isPast   = isToday && !bookedSlots.includes(slot) && disabled;
                      const selected = formData.appointmentTime === slot;
                      return (
                        <button
                          key={slot}
                          type="button"
                          className={`slot-btn ${disabled ? 'slot-taken' : ''} ${selected ? 'slot-selected' : ''}`}
                          onClick={() => handleSlotSelect(slot)}
                          disabled={disabled}
                          title={bookedSlots.includes(slot) ? 'This slot is already booked' : isPast ? 'This time has already passed' : ''}
                        >
                          {formatSlot(slot)}
                          {bookedSlots.includes(slot) && <span className="slot-taken-label">Taken</span>}
                          {isPast && <span className="slot-taken-label">Past</span>}
                        </button>
                      );
                    })}
                  </div>
                </>
              )}

              <label>Additional Notes (optional)</label>
              <textarea
                name="notes"
                placeholder="Any additional information for the clinic..."
                value={formData.notes}
                onChange={handleChange}
              />

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

            <div className="slot-legend">
              <span className="legend-item"><span className="legend-dot dot-available"></span>Available</span>
              <span className="legend-item"><span className="legend-dot dot-selected"></span>Selected</span>
              <span className="legend-item"><span className="legend-dot dot-taken"></span>Taken</span>
            </div>

            <div className="form-actions">
              <button type="button" className="btn-secondary" onClick={() => navigate('/dashboard')}>
                Cancel
              </button>
              <button
                type="submit"
                className="btn-primary"
                disabled={loading || !formData.appointmentTime || availableCount === 0}
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