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
import { financeApi } from '../../api/finance'
import DataTable from '../../components/common/DataTable'
import FormDialog from '../../components/common/FormDialog'
import ConfirmDialog from '../../components/common/ConfirmDialog'
import { useAuthStore } from '../../store/authStore'

const Expenses = () => {
  const { user } = useAuthStore()
  const queryClient = useQueryClient()
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [openDialog, setOpenDialog] = useState(false)
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false)
  const [selectedExpense, setSelectedExpense] = useState(null)
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    category: 'OPERATIONAL',
    amount: '',
    expenseDate: new Date().toISOString().split('T')[0],
    vendorName: '',
    vendorDetails: '',
  })

  const { data, isLoading, error } = useQuery({
    queryKey: ['expenses', page, pageSize],
    queryFn: () =>
      financeApi.getExpenses({ page, size: pageSize }).then(res => res.data.data),
  })

  const createMutation = useMutation({
    mutationFn: (data) => financeApi.createExpense(data),
    onSuccess: () => {
      queryClient.invalidateQueries(['expenses'])
      setOpenDialog(false)
      resetForm()
    },
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => financeApi.updateExpense(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['expenses'])
      setOpenDialog(false)
      setSelectedExpense(null)
      resetForm()
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (id) => financeApi.deleteExpense(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['expenses'])
      setOpenDeleteDialog(false)
      setSelectedExpense(null)
    },
  })

  const approveMutation = useMutation({
    mutationFn: ({ id, data }) => financeApi.approveExpense(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['expenses'])
    },
  })

  const resetForm = () => {
    setFormData({
      title: '',
      description: '',
      category: 'OPERATIONAL',
      amount: '',
      expenseDate: new Date().toISOString().split('T')[0],
      vendorName: '',
      vendorDetails: '',
    })
  }

  const handleEdit = (expense) => {
    setSelectedExpense(expense)
    setFormData({
      title: expense.title || '',
      description: expense.description || '',
      category: expense.category || 'OPERATIONAL',
      amount: expense.amount || '',
      expenseDate: expense.expenseDate
        ? new Date(expense.expenseDate).toISOString().split('T')[0]
        : new Date().toISOString().split('T')[0],
      vendorName: expense.vendorName || '',
      vendorDetails: expense.vendorDetails || '',
    })
    setOpenDialog(true)
  }

  const handleDelete = (expense) => {
    setSelectedExpense(expense)
    setOpenDeleteDialog(true)
  }

  const handleApprove = (expense) => {
    approveMutation.mutate({
      id: expense.id,
      data: { approvalNotes: 'Approved' },
    })
  }

  const handleSubmit = () => {
    const submitData = {
      ...formData,
      amount: parseFloat(formData.amount),
    }

    if (selectedExpense) {
      updateMutation.mutate({ id: selectedExpense.id, data: submitData })
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
      case 'PAID':
        return 'info'
      default:
        return 'default'
    }
  }

  const columns = [
    { field: 'title', headerName: 'Title' },
    {
      field: 'category',
      headerName: 'Category',
    },
    {
      field: 'amount',
      headerName: 'Amount',
      render: (value) => `$${parseFloat(value || 0).toFixed(2)}`,
    },
    {
      field: 'status',
      headerName: 'Status',
      render: (value) => (
        <Chip label={value} color={getStatusColor(value)} size="small" />
      ),
    },
    {
      field: 'expenseDate',
      headerName: 'Date',
      render: (value) => (value ? new Date(value).toLocaleDateString() : '-'),
    },
    { field: 'requestedByName', headerName: 'Requested By' },
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
    return <Alert severity="error">Failed to load expenses</Alert>
  }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Expenses</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => {
            setSelectedExpense(null)
            resetForm()
            setOpenDialog(true)
          }}
        >
          Add Expense
        </Button>
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
        emptyMessage="No expenses found"
      />

      <FormDialog
        open={openDialog}
        title={selectedExpense ? 'Edit Expense' : 'Add Expense'}
        onClose={() => {
          setOpenDialog(false)
          setSelectedExpense(null)
          resetForm()
        }}
        onSubmit={handleSubmit}
        loading={createMutation.isLoading || updateMutation.isLoading}
      >
        <Grid container spacing={2}>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Title"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              required
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Description"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              multiline
              rows={3}
              required
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              select
              label="Category"
              value={formData.category}
              onChange={(e) => setFormData({ ...formData, category: e.target.value })}
              required
            >
              <MenuItem value="OPERATIONAL">Operational</MenuItem>
              <MenuItem value="MAINTENANCE">Maintenance</MenuItem>
              <MenuItem value="UTILITIES">Utilities</MenuItem>
              <MenuItem value="SUPPLIES">Supplies</MenuItem>
              <MenuItem value="MARKETING">Marketing</MenuItem>
              <MenuItem value="TRAVEL">Travel</MenuItem>
              <MenuItem value="OTHER">Other</MenuItem>
            </TextField>
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Amount"
              type="number"
              value={formData.amount}
              onChange={(e) => setFormData({ ...formData, amount: e.target.value })}
              required
              inputProps={{ step: '0.01' }}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Expense Date"
              type="date"
              value={formData.expenseDate}
              onChange={(e) => setFormData({ ...formData, expenseDate: e.target.value })}
              InputLabelProps={{ shrink: true }}
              required
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Vendor Name"
              value={formData.vendorName}
              onChange={(e) => setFormData({ ...formData, vendorName: e.target.value })}
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Vendor Details"
              value={formData.vendorDetails}
              onChange={(e) => setFormData({ ...formData, vendorDetails: e.target.value })}
              multiline
              rows={2}
            />
          </Grid>
        </Grid>
      </FormDialog>

      <ConfirmDialog
        open={openDeleteDialog}
        title="Delete Expense"
        description={`Are you sure you want to delete "${selectedExpense?.title}"?`}
        onClose={() => {
          setOpenDeleteDialog(false)
          setSelectedExpense(null)
        }}
        onConfirm={() => deleteMutation.mutate(selectedExpense?.id)}
        confirmText="Delete"
      />
    </Box>
  )
}

export default Expenses
