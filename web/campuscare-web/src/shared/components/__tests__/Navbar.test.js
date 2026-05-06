import React from 'react';
import { render, screen, fireEvent } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import Navbar from '../Navbar';

beforeEach(() => {
  localStorage.setItem('userFirstName', 'John');
  localStorage.setItem('userLastName', 'Doe');
});

afterEach(() => {
  localStorage.clear();
});

test('renders student navigation links', () => {
  render(<MemoryRouter><Navbar role="STUDENT" /></MemoryRouter>);
  expect(screen.getByText('CampusCare')).toBeInTheDocument();
  expect(screen.getByText('Dashboard')).toBeInTheDocument();
  expect(screen.getByText('Book Appointment')).toBeInTheDocument();
  expect(screen.getByText('Profile')).toBeInTheDocument();
  expect(screen.queryByText('Admin Panel')).not.toBeInTheDocument();
});

test('renders admin navigation links', () => {
  render(<MemoryRouter><Navbar role="ADMIN" /></MemoryRouter>);
  expect(screen.getByText('Admin Panel')).toBeInTheDocument();
  expect(screen.getByText('Profile')).toBeInTheDocument();
});

test('logout button exists and does not crash', () => {
  render(<MemoryRouter><Navbar role="STUDENT" /></MemoryRouter>);
  const logoutBtn = screen.getByText('Log Out');
  expect(logoutBtn).toBeInTheDocument();
  expect(() => fireEvent.click(logoutBtn)).not.toThrow();
});