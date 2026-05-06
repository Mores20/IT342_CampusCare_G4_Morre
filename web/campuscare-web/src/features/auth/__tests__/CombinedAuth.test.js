import React from 'react';
import { render, screen, fireEvent, waitFor } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import CombinedAuth from '../CombinedAuth';

jest.mock('../../../shared/services/api', () => ({
  apiRequest: jest.fn(),
}));
jest.mock('@react-oauth/google', () => ({
  GoogleLogin: () => <button>Google Sign In</button>,
}));

const mockNavigate = jest.fn();
jest.mock('react-router-dom', () => ({
  ...jest.requireActual('react-router-dom'),
  useNavigate: () => mockNavigate,
}));

const { apiRequest } = require('../../../shared/services/api');

beforeEach(() => {
  jest.clearAllMocks();
});

test('renders login form by default', () => {
  render(<MemoryRouter><CombinedAuth /></MemoryRouter>);
  expect(screen.getByPlaceholderText(/email/i)).toBeInTheDocument();
  expect(screen.getByPlaceholderText(/password/i)).toBeInTheDocument();
});

test('shows error on failed login', async () => {
  apiRequest.mockRejectedValue(new Error('Invalid credentials'));
  render(<MemoryRouter><CombinedAuth /></MemoryRouter>);
  fireEvent.change(screen.getByPlaceholderText(/email/i), { target: { value: 'test@t.com' } });
  fireEvent.change(screen.getByPlaceholderText(/password/i), { target: { value: 'wrong' } });
  // Click the submit button (type="submit"), not the tab
const buttons = screen.getAllByText('Login');
const submitButton = buttons.find(b => b.className.includes('auth-submit'));
fireEvent.click(submitButton);
  await waitFor(() => {
    expect(screen.getByText(/network request failed/i)).toBeInTheDocument();
  });
});