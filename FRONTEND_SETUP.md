# Frontend Setup Guide

## Quick Start

```bash
cd frontend
npm install
npm run dev
```

The frontend will be available at `http://localhost:3000`

## What's Implemented

### ✅ Core Infrastructure
- React 18 with Vite
- Material-UI theme and components
- React Router for navigation
- React Query for API state management
- Zustand for auth state
- Axios with interceptors

### ✅ Authentication
- Login page
- Register page
- Forgot Password page
- JWT token management
- Protected routes
- Public routes

### ✅ Layout
- Main layout with sidebar
- Role-based menu navigation
- Responsive design
- Header with user menu

### ✅ Dashboard
- Role-specific statistics
- Dynamic stat cards based on user role
- Integration with dashboard API

### ✅ API Integration
- Complete API client setup
- All module API endpoints defined
- Error handling
- Token refresh handling

### ✅ Pages Structure
All pages created with placeholder content:
- Academic: Courses, Enrollments, Attendance, Grades, Exams, Certificates
- Finance: Fee Structures, Invoices, Payments, Expenses, Reports
- Marketing: Leads, Campaigns
- HR: Employees, Leaves
- User: Profile

## Next Steps

1. **Enhance Course Page** - Add CRUD operations, filters, search
2. **Build Form Components** - Reusable form components with validation
3. **Add Data Tables** - Implement tables with pagination, sorting, filtering
4. **Create Modals** - Reusable modal components for forms
5. **Add Charts** - Integrate Recharts for dashboard visualizations
6. **File Upload** - Profile photo upload functionality
7. **PDF Downloads** - Integrate PDF download for certificates, invoices, etc.

## Features Ready to Build

- All API endpoints are defined and ready to use
- Authentication flow is complete
- Routing is set up
- State management is configured
- Theme and styling are ready

## Development

```bash
# Start dev server
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview
```

## API Configuration

The frontend is configured to proxy API requests to `http://localhost:8080/api` when running in development. Update `vite.config.js` if your backend runs on a different port.

