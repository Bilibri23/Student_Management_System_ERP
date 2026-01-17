# ERP System Frontend

React-based frontend for the ERP System built with Vite, Material-UI, and React Query.

## Tech Stack

- **React 18** - UI library
- **Vite** - Build tool
- **React Router v6** - Routing
- **Material-UI (MUI)** - Component library
- **React Query (TanStack Query)** - Server state management
- **Zustand** - Client state management
- **Axios** - HTTP client
- **React Hook Form** - Form management

## Getting Started

### Prerequisites

- Node.js 18+ and npm/yarn

### Installation

```bash
# Install dependencies
npm install

# Start development server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

The app will be available at `http://localhost:3000`

## Project Structure

```
frontend/
├── src/
│   ├── api/              # API service layer
│   ├── components/       # Reusable components
│   │   ├── guards/      # Route guards
│   │   └── layout/      # Layout components
│   ├── pages/           # Page components
│   │   ├── auth/        # Authentication pages
│   │   ├── dashboard/   # Dashboard
│   │   ├── academic/    # Academic module pages
│   │   ├── finance/     # Finance module pages
│   │   ├── marketing/   # Marketing module pages
│   │   ├── hr/          # HR module pages
│   │   └── user/        # User pages
│   ├── store/           # State management
│   ├── theme.js         # MUI theme configuration
│   ├── App.jsx          # Main app component
│   └── main.jsx         # Entry point
├── public/              # Static assets
├── package.json
└── vite.config.js
```

## Features

- ✅ Authentication (Login, Register, Forgot Password)
- ✅ Role-based access control
- ✅ Dashboard with role-specific statistics
- ✅ Responsive design
- ✅ API integration
- ✅ JWT token management
- ✅ Protected routes

## Pages Created

### Auth Pages
- ✅ Login
- ✅ Register
- ✅ Forgot Password

### Main Pages
- ✅ Dashboard (role-specific)
- ✅ Layout with sidebar navigation

### TODO: Additional Pages
- Academic: Courses, Enrollments, Attendance, Grades, Exams, Certificates
- Finance: Fee Structures, Invoices, Payments, Expenses, Reports
- Marketing: Leads, Campaigns
- HR: Employees, Leaves
- User: Profile

## API Integration

All API calls are configured in the `src/api/` directory:
- `axios.js` - Axios instance with interceptors
- `auth.js` - Authentication APIs
- `academic.js` - Academic module APIs
- `finance.js` - Finance module APIs
- `marketing.js` - Marketing module APIs
- `hr.js` - HR module APIs
- `user.js` - User APIs
- `dashboard.js` - Dashboard APIs

## State Management

- **Zustand** for auth state (user, tokens, authentication status)
- **React Query** for server state (API data, caching, refetching)

## Environment Variables

Create a `.env` file:

```env
VITE_API_BASE_URL=http://localhost:8080/api
```

## Build & Deploy

```bash
# Build for production
npm run build

# The build output will be in the `dist/` directory
```

Deploy the `dist/` folder to your hosting provider (Netlify, Vercel, AWS S3, etc.)

