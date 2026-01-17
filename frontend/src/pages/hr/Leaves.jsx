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
} from '@mui/material'
import { Add as AddIcon } from '@mui/icons-material'
import { hrApi } from '../../api/hr'
import DataTable from '../../components/common/DataTable'
import FormDialog from '../../components/common/FormDialog'
import ConfirmDialog from '../../components/common/ConfirmDialog'
import { useAuthStore } from '../../store/authStore'

const Leaves = () => {
  const { user } = useAuthStore()
  const queryClient = useQueryClient()
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [openDialog, setOpenDialog] = useState(false)
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false)
  const [selectedLeave, setSelectedLeave] = useState(null)
  const [statusFilter, setStatusFilter] = useState('')
  const [formData, setFormData] = useState({
    employeeId: user?.id || '',
    leaveType: 'ANNUAL',
    startDate: new Date().toISOString().split('T')[0],
    endDate: '',
    reason: '',
    notes: '',
    contactDuringLeave: '',
    coveringPersonId: '',
  })

  const { data, isLoading, error } = useQuery({
    queryKey: ['leaves', page, pageSize, statusFilter],
    queryFn: () => {
      const params = { page, size: pageSize }
      if (user?.role === 'STUDENT') {
        params.employeeId = user.id
      }
      if (statusFilter) {
        params.status = statusFilter
      }
      return hrApi.getLeaves(params).then(res => res.data.data)
    },
  })

  const createMutation = useMutation({
    mutationFn: (data) => hrApi.requestLeave(data),
    onSuccess: () => {
      queryClient.invalidateQueries(['leaves'])
      setOpenDialog(false)
      resetForm()
    },
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => hrApi.updateLeave(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['leaves'])
      setOpenDialog(false)
      setSelectedLeave(null)
      resetForm()
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (id) => hrApi.deleteLeave(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['leaves'])
      setOpenDeleteDialog(false)
      setSelectedLeave(null)
    },
  })

  const approveMutation = useMutation({
    mutationFn: ({ id, data }) => hrApi.approveLeave(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['leaves'])
    },
  })

  const rejectMutation = useMutation({
    mutationFn: ({ id, data }) => hrApi.rejectLeave(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['leaves'])
    },
  })

  const resetForm = () => {
    setFormData({
      employeeId: user?.id || '',
      leaveType: 'ANNUAL',
      startDate: new Date().toISOString().split('T')[0],
      endDate: '',
      reason: '',
      notes: '',
      contactDuringLeave: '',
      coveringPersonId: '',
    })
  }

  const handleEdit = (leave) => {
    setSelectedLeave(leave)
    setFormData({
      employeeId: leave.employeeId || user?.id || '',
      leaveType: leave.leaveType || 'ANNUAL',
      startDate: leave.startDate
        ? new Date(leave.startDate).toISOString().split('T')[0]
        : new Date().toISOString().split('T')[0],
      endDate: leave.endDate
        ? new Date(leave.endDate).toISOString().split('T')[0]
        : '',
      reason: leave.reason || '',
      notes: leave.notes || '',
      contactDuringLeave: leave.contactDuringLeave || '',
      coveringPersonId: leave.coveringPersonId || '',
    })
    setOpenDialog(true)
  }

  const handleDelete = (leave) => {
    setSelectedLeave(leave)
    setOpenDeleteDialog(true)
  }

  const handleApprove = (leave) => {
    approveMutation.mutate({
      id: leave.id,
      data: { approvalNotes: 'Approved' },
    })
  }

  const handleReject = (leave) => {
    rejectMutation.mutate({
      id: leave.id,
      data: { rejectionReason: 'Rejected' },
    })
  }

  const handleSubmit = () => {
    const submitData = {
      ...formData,
      employeeId: parseInt(formData.employeeId),
      coveringPersonId: formData.coveringPersonId ? parseInt(formData.coveringPersonId) : null,
    }

    if (selectedLeave) {
      updateMutation.mutate({ id: selectedLeave.id, data: submitData })
    } else {
      createMutation.mutate(submitData)
    }
  }

  const getStatusColor = (status) => {
    switch (status) {
      case 'APPROVED':
        return 'success'
      case 'PENDING':
        return 'warning'
      case 'REJECTED':
        return 'error'
      case 'CANCELLED':
        return 'default'
      default:
        return 'default'
    }
  }

  const columns = [
    { field: 'employeeName', headerName: 'Employee' },
    {
      field: 'leaveType',
      headerName: 'Type',
    },
    {
      field: 'status',
      headerName: 'Status',
      render: (value) => (
        <Chip label={value} color={getStatusColor(value)} size="small" />
      ),
    },
    {
      field: 'startDate',
      headerName: 'Start Date',
      render: (value) => (value ? new Date(value).toLocaleDateString() : '-'),
    },
    {
      field: 'endDate',
      headerName: 'End Date',
      render: (value) => (value ? new Date(value).toLocaleDateString() : '-'),
    },
    { field: 'totalDays', headerName: 'Days' },
    {
      field: 'requestedDate',
      headerName: 'Requested',
      render: (value) => (value ? new Date(value).toLocaleDateString() : '-'),
    },
  ]

  const rows = data?.content || []

  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" minHeight="400px">
        <CircularProgress />
      </Box>
    )
  }

  if (error) {
    return <Alert severity="error">Failed to load leaves</Alert>
  }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Leave Management</Typography>
        {user?.role !== 'STUDENT' && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={() => {
              setSelectedLeave(null)
              resetForm()
              setOpenDialog(true)
            }}
          >
            Request Leave
          </Button>
        )}
      </Box>

      <Box mb={2}>
        <TextField
          select
          label="Filter by Status"
          value={statusFilter}
          onChange={(e) => setStatusFilter(e.target.value)}
          size="small"
          sx={{ minWidth: 200 }}
        >
          <MenuItem value="">All</MenuItem>
          <MenuItem value="PENDING">Pending</MenuItem>
          <MenuItem value="APPROVED">Approved</MenuItem>
          <MenuItem value="REJECTED">Rejected</MenuItem>
          <MenuItem value="CANCELLED">Cancelled</MenuItem>
        </TextField>
      </Box>

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
        emptyMessage="No leaves found"
      />

      <FormDialog
        open={openDialog}
        title={selectedLeave ? 'Edit Leave Request' : 'Request Leave'}
        onClose={() => {
          setOpenDialog(false)
          setSelectedLeave(null)
          resetForm()
        }}
        onSubmit={handleSubmit}
        loading={createMutation.isLoading || updateMutation.isLoading}
      >
        <Grid container spacing={2}>
          {user?.role !== 'STUDENT' && (
            <Grid item xs={12}>
              <TextField
                fullWidth
                label="Employee ID"
                type="number"
                value={formData.employeeId}
                onChange={(e) => setFormData({ ...formData, employeeId: e.target.value })}
                required
              />
            </Grid>
          )}
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              select
              label="Leave Type"
              value={formData.leaveType}
              onChange={(e) => setFormData({ ...formData, leaveType: e.target.value })}
              required
            >
              <MenuItem value="ANNUAL">Annual</MenuItem>
              <MenuItem value="SICK">Sick</MenuItem>
              <MenuItem value="CASUAL">Casual</MenuItem>
              <MenuItem value="MATERNITY">Maternity</MenuItem>
              <MenuItem value="PATERNITY">Paternity</MenuItem>
              <MenuItem value="UNPAID">Unpaid</MenuItem>
            </TextField>
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Total Days"
              type="number"
              value={
                formData.startDate && formData.endDate
                  ? Math.ceil(
                      (new Date(formData.endDate) - new Date(formData.startDate)) /
                        (1000 * 60 * 60 * 24)
                    ) + 1
                  : ''
              }
              disabled
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Start Date"
              type="date"
              value={formData.startDate}
              onChange={(e) => setFormData({ ...formData, startDate: e.target.value })}
              InputLabelProps={{ shrink: true }}
              required
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="End Date"
              type="date"
              value={formData.endDate}
              onChange={(e) => setFormData({ ...formData, endDate: e.target.value })}
              InputLabelProps={{ shrink: true }}
              required
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Reason"
              value={formData.reason}
              onChange={(e) => setFormData({ ...formData, reason: e.target.value })}
              multiline
              rows={3}
              required
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Notes"
              value={formData.notes}
              onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
              multiline
              rows={2}
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Contact During Leave"
              value={formData.contactDuringLeave}
              onChange={(e) => setFormData({ ...formData, contactDuringLeave: e.target.value })}
            />
          </Grid>
        </Grid>
      </FormDialog>

      <ConfirmDialog
        open={openDeleteDialog}
        title="Delete Leave Request"
        description={`Are you sure you want to delete this leave request?`}
        onClose={() => {
          setOpenDeleteDialog(false)
          setSelectedLeave(null)
        }}
        onConfirm={() => deleteMutation.mutate(selectedLeave?.id)}
        confirmText="Delete"
      />
    </Box>
  )
}

export default Leaves
