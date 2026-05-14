import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Profile from '../Profile';

jest.mock('../../../shared/services/api', () => ({
  apiRequest: jest.fn(),
}));
const { apiRequest } = require('../../../shared/services/api');

beforeEach(() => {
  jest.clearAllMocks();
  localStorage.setItem('authToken', 'mock-token');
  localStorage.setItem('userRole', 'STUDENT');
});

afterEach(() => {
  localStorage.clear();
});

test('loads and displays profile data', async () => {
  apiRequest.mockResolvedValueOnce({
    firstName: 'John',
    lastName: 'Doe',
    email: 'john@test.com',
    role: 'STUDENT',
  });
  render(<MemoryRouter><Profile /></MemoryRouter>);
  await waitFor(() => {
    expect(screen.getByText('John Doe')).toBeInTheDocument();
  });
});

test('shows password strength indicator', async () => {
  apiRequest.mockResolvedValueOnce({
    firstName: 'John',
    lastName: 'Doe',
    email: 'john@test.com',
    role: 'STUDENT',
  });
  render(<MemoryRouter><Profile /></MemoryRouter>);
  
  // Wait for profile to load, then switch to Security tab
  await waitFor(() => {
    expect(screen.getByText('John Doe')).toBeInTheDocument();
  });
  
  // Click the Security tab
  fireEvent.click(screen.getByText(/security/i));
  
  // Now find the new password field
 const newPassInput = screen.getByPlaceholderText(/enter new password/i);
  fireEvent.change(newPassInput, { target: { value: 'StrongPass123!' } });
  
    expect(screen.getByText('Strong')).toBeInTheDocument();
});