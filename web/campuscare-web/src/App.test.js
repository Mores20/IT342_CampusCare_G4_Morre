import { render, screen } from '@testing-library/react';
import App from './App';

// Mock Google OAuth provider
jest.mock('@react-oauth/google', () => ({
  GoogleOAuthProvider: ({ children }) => <div>{children}</div>,
  GoogleLogin: () => <button>Google Sign In</button>,
}));

beforeEach(() => {
  Object.defineProperty(window, 'localStorage', {
    value: {
      getItem: jest.fn(() => null),
      setItem: jest.fn(() => null),
      removeItem: jest.fn(() => null),
      clear: jest.fn(() => null),
    },
    writable: true,
  });
});

test('renders login page when no token', () => {
  render(<App />);
  expect(screen.getByText(/welcome to/i)).toBeInTheDocument();
  expect(screen.getByText('CampusCare')).toBeInTheDocument();
});