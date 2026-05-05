import { BrowserRouter as Router, Routes, Route, Navigate } from "react-router-dom";
import "./App.css";

// Feature imports
import CombinedAuth from "./features/auth/CombinedAuth";
import Dashboard from "./features/dashboard/Dashboard";
import BookAppointment from "./features/dashboard/BookAppointment";
import AdminDashboard from "./features/admin/AdminDashboard";
import Profile from "./features/profile/Profile";

// Protected Route - checks authentication only
const PrivateRoute = ({ children }) => {
  const token = localStorage.getItem('authToken');
  return token ? children : <Navigate to="/login" />;
};

// Admin Route - checks for ADMIN role
const AdminRoute = ({ children }) => {
  const token = localStorage.getItem('authToken');
  const userRole = localStorage.getItem('userRole');
  
  if (!token) {
    return <Navigate to="/login" />;
  }
  
  if (userRole !== 'ADMIN') {
    return <Navigate to="/dashboard" />;
  }
  
  return children;
};

function App() {
  return (
    <Router>
      <div className="App">
        <Routes>
          <Route path="/" element={<Navigate to="/login" />} />
          <Route path="/login" element={<CombinedAuth />} />
          <Route path="/register" element={<CombinedAuth />} />
          
          <Route path="/profile" element={
             <PrivateRoute><Profile /></PrivateRoute>
          } />

          {/* User routes */}
          <Route path="/dashboard" element={
            <PrivateRoute><Dashboard /></PrivateRoute>
          } />
          <Route path="/book" element={
            <PrivateRoute><BookAppointment /></PrivateRoute>
          } />

          {/* Admin routes */}
          <Route path="/admin" element={
            <AdminRoute><AdminDashboard /></AdminRoute>
          } />
          <Route path="/admin/dashboard" element={
            <AdminRoute><AdminDashboard /></AdminRoute>
          } />
        </Routes>
      </div>
    </Router>
  );
}

export default App;