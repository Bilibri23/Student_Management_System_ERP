import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  Box,
  Typography,
  Button,
  CircularProgress,
  Alert,
  Chip,
  TextField,
  MenuItem,
  Grid,
  Switch,
  FormControlLabel,
  Card,
  CardContent,
  Avatar,
  Skeleton,
} from '@mui/material'
import {
  Add as AddIcon,
  People as PeopleIcon,
  PersonOff as InactiveIcon,
  Badge as BadgeIcon,
} from '@mui/icons-material'
import { hrApi } from '../../api/hr'
import DataTable from '../../components/common/DataTable'
import FormDialog from '../../components/common/FormDialog'
import ConfirmDialog from '../../components/common/ConfirmDialog'
import PageHeader from '../../components/common/PageHeader'

const Employees = () => {
  const queryClient = useQueryClient()
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [openDialog, setOpenDialog] = useState(false)
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false)
  const [selectedEmployee, setSelectedEmployee] = useState(null)
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    firstName: '',
    lastName: '',
    phone: '',
    address: '',
    role: 'ACADEMIC_STAFF',
    active: true,
  })

  const { data, isLoading, error } = useQuery({
    queryKey: ['employees', page, pageSize],
    queryFn: () =>
      hrApi.getEmployees({ page, size: pageSize }).then(res => res.data.data),
  })

  const createMutation = useMutation({
    mutationFn: (data) => hrApi.addEmployee(data),
    onSuccess: () => {
      queryClient.invalidateQueries(['employees'])
      setOpenDialog(false)
      resetForm()
    },
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => hrApi.updateEmployee(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['employees'])
      setOpenDialog(false)
      setSelectedEmployee(null)
      resetForm()
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (id) => hrApi.deleteEmployee(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['employees'])
      setOpenDeleteDialog(false)
      setSelectedEmployee(null)
    },
  })

  const resetForm = () => {
    setFormData({
      username: '',
      email: '',
      password: '',
      firstName: '',
      lastName: '',
      phone: '',
      address: '',
      role: 'ACADEMIC_STAFF',
      active: true,
    })
  }

  const handleEdit = (employee) => {
    setSelectedEmployee(employee)
    setFormData({
      username: employee.username || '',
      email: employee.email || '',
      password: '',
      firstName: employee.firstName || '',
      lastName: employee.lastName || '',
      phone: employee.phone || '',
      address: employee.address || '',
      role: employee.role || 'ACADEMIC_STAFF',
      active: employee.active ?? true,
    })
    setOpenDialog(true)
  }

  const handleDelete = (employee) => {
    setSelectedEmployee(employee)
    setOpenDeleteDialog(true)
  }

  const handleSubmit = () => {
    const submitData = { ...formData }
    if (selectedEmployee && !submitData.password) {
      delete submitData.password
    }

    if (selectedEmployee) {
      updateMutation.mutate({ id: selectedEmployee.id, data: submitData })
    } else {
      createMutation.mutate(submitData)
    }
  }

  const columns = [
    {
      field: 'fullName',
      headerName: 'Name',
      render: (_, row) => `${row.firstName || ''} ${row.lastName || ''}`.trim() || row.fullName,
    },
    { field: 'email', headerName: 'Email' },
    { field: 'phone', headerName: 'Phone' },
    {
      field: 'role',
      headerName: 'Role',
    },
    {
      field: 'active',
      headerName: 'Status',
      render: (value) => (
        <Chip label={value ? 'Active' : 'Inactive'} color={value ? 'success' : 'default'} size="small" />
      ),
    },
    {
      field: 'createdAt',
      headerName: 'Created',
      render: (value) => (value ? new Date(value).toLocaleDateString() : '-'),
    },
  ]

  const rows = data?.content || []

  const statsCards = [
    {
      title: 'Total Employees',
      value: data?.totalElements || 0,
      icon: <PeopleIcon />,
      color: '#6366F1',
    },
    {
      title: 'Active',
      value: rows.filter(e => e.active).length,
      icon: <BadgeIcon />,
      color: '#10B981',
    },
    {
      title: 'Inactive',
      value: rows.filter(e => !e.active).length,
      icon: <InactiveIcon />,
      color: '#EF4444',
    },
  ]

  if (isLoading) {
    return (
      <Box>
        <Skeleton variant="rounded" height={80} sx={{ mb: 3, borderRadius: 3 }} />
        <Grid container spacing={3} sx={{ mb: 4 }}>
          {[1, 2, 3].map((i) => (
            <Grid item xs={12} sm={4} key={i}>
              <Skeleton variant="rounded" height={100} sx={{ borderRadius: 3 }} />
            </Grid>
          ))}
        </Grid>
        <Skeleton variant="rounded" height={400} sx={{ borderRadius: 3 }} />
      </Box>
    )
  }

  if (error) {
    return <Alert severity="error" sx={{ borderRadius: 3 }}>Failed to load employees</Alert>
  }

  return (
    <Box>
      <PageHeader
        title="Employees"
        subtitle="Manage staff and employee records"
        action={() => { setSelectedEmployee(null); resetForm(); setOpenDialog(true) }}
        actionLabel="Add Employee"
        badge={`${data?.totalElements || 0} total`}
      />

      {/* Stats Cards */}
      <Grid container spacing={3} sx={{ mb: 4 }}>
        {statsCards.map((stat, index) => (
          <Grid item xs={12} sm={4} key={index}>
            <Card
              sx={{
                background: `linear-gradient(135deg, ${stat.color}10 0%, ${stat.color}05 100%)`,
                border: `1px solid ${stat.color}20`,
                transition: 'all 0.3s ease',
                '&:hover': {
                  transform: 'translateY(-2px)',
                  boxShadow: `0 8px 24px ${stat.color}20`,
                },
              }}
            >
              <CardContent sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Avatar sx={{ width: 48, height: 48, bgcolor: `${stat.color}20`, color: stat.color }}>
                  {stat.icon}
                </Avatar>
                <Box>
                  <Typography variant="h4" fontWeight={700} color={stat.color}>
                    {stat.value}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {stat.title}
                  </Typography>
                </Box>
              </CardContent>
            </Card>
          </Grid>
        ))}
      </Grid>

      <DataTable
        columns={columns}
        rows={rows}
        page={page}
        pageSize={pageSize}
        total={data?.totalElements || 0}
        onPageChange={setPage}
        onPageSizeChange={setPageSize}
        actions={{
          edit: handleEdit,
          delete: handleDelete,
        }}
        emptyMessage="No employees found"
      />

      <FormDialog
        open={openDialog}
        title={selectedEmployee ? 'Edit Employee' : 'Add Employee'}
        onClose={() => {
          setOpenDialog(false)
          setSelectedEmployee(null)
          resetForm()
        }}
        onSubmit={handleSubmit}
        loading={createMutation.isLoading || updateMutation.isLoading}
      >
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="First Name"
              value={formData.firstName}
              onChange={(e) => setFormData({ ...formData, firstName: e.target.value })}
              required
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Last Name"
              value={formData.lastName}
              onChange={(e) => setFormData({ ...formData, lastName: e.target.value })}
              required
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Username"
              value={formData.username}
              onChange={(e) => setFormData({ ...formData, username: e.target.value })}
              required
              disabled={!!selectedEmployee}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Email"
              type="email"
              value={formData.email}
              onChange={(e) => setFormData({ ...formData, email: e.target.value })}
              required
            />
          </Grid>
          {!selectedEmployee && (
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Password"
                type="password"
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                required
              />
            </Grid>
          )}
          {selectedEmployee && (
            <Grid item xs={12} sm={6}>
              <TextField
                fullWidth
                label="Password (leave blank to keep current)"
                type="password"
                value={formData.password}
                onChange={(e) => setFormData({ ...formData, password: e.target.value })}
              />
            </Grid>
          )}
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Phone"
              value={formData.phone}
              onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              select
              label="Role"
              value={formData.role}
              onChange={(e) => setFormData({ ...formData, role: e.target.value })}
              required
            >
              <MenuItem value="ACADEMIC_STAFF">Academic Staff</MenuItem>
              <MenuItem value="FINANCE_STAFF">Finance Staff</MenuItem>
              <MenuItem value="HR_OFFICER">HR Officer</MenuItem>
              <MenuItem value="ADMIN">Admin</MenuItem>
            </TextField>
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Address"
              value={formData.address}
              onChange={(e) => setFormData({ ...formData, address: e.target.value })}
              multiline
              rows={2}
            />
          </Grid>
          {selectedEmployee && (
            <Grid item xs={12}>
              <FormControlLabel
                control={
                  <Switch
                    checked={formData.active}
                    onChange={(e) => setFormData({ ...formData, active: e.target.checked })}
                  />
                }
                label="Active"
              />
            </Grid>
          )}
        </Grid>
      </FormDialog>

      <ConfirmDialog
        open={openDeleteDialog}
        title="Delete Employee"
        description={`Are you sure you want to delete employee "${selectedEmployee?.firstName} ${selectedEmployee?.lastName}"?`}
        onClose={() => {
          setOpenDeleteDialog(false)
          setSelectedEmployee(null)
        }}
        onConfirm={() => deleteMutation.mutate(selectedEmployee?.id)}
        confirmText="Delete"
      />
    </Box>
  )
}

export default Employees
