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
} from '@mui/material'
import { Add as AddIcon } from '@mui/icons-material'
import { financeApi } from '../../api/finance'
import DataTable from '../../components/common/DataTable'
import FormDialog from '../../components/common/FormDialog'
import ConfirmDialog from '../../components/common/ConfirmDialog'

const FeeStructures = () => {
  const queryClient = useQueryClient()
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [openDialog, setOpenDialog] = useState(false)
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false)
  const [selectedFee, setSelectedFee] = useState(null)
  const [formData, setFormData] = useState({
    program: '',
    semester: '',
    academicYear: '',
    name: '',
    description: '',
    amount: '',
    isActive: true,
    isRequired: true,
    recurring: false,
    dueDayOfMonth: '',
    orderIndex: 0,
  })

  const { data, isLoading, error } = useQuery({
    queryKey: ['feeStructures', page, pageSize],
    queryFn: () =>
      financeApi.getFeeStructures({ page, size: pageSize }).then(res => res.data.data),
  })

  const createMutation = useMutation({
    mutationFn: (data) => financeApi.createFeeStructure(data),
    onSuccess: () => {
      queryClient.invalidateQueries(['feeStructures'])
      setOpenDialog(false)
      resetForm()
    },
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => financeApi.updateFeeStructure(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['feeStructures'])
      setOpenDialog(false)
      setSelectedFee(null)
      resetForm()
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (id) => financeApi.deleteFeeStructure(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['feeStructures'])
      setOpenDeleteDialog(false)
      setSelectedFee(null)
    },
  })

  const resetForm = () => {
    setFormData({
      program: '',
      semester: '',
      academicYear: '',
      name: '',
      description: '',
      amount: '',
      isActive: true,
      isRequired: true,
      recurring: false,
      dueDayOfMonth: '',
      orderIndex: 0,
    })
  }

  const handleEdit = (fee) => {
    setSelectedFee(fee)
    setFormData({
      program: fee.program || '',
      semester: fee.semester || '',
      academicYear: fee.academicYear || '',
      name: fee.name || '',
      description: fee.description || '',
      amount: fee.amount || '',
      isActive: fee.isActive ?? true,
      isRequired: fee.isRequired ?? true,
      recurring: fee.recurring ?? false,
      dueDayOfMonth: fee.dueDayOfMonth || '',
      orderIndex: fee.orderIndex || 0,
    })
    setOpenDialog(true)
  }

  const handleDelete = (fee) => {
    setSelectedFee(fee)
    setOpenDeleteDialog(true)
  }

  const handleSubmit = () => {
    const submitData = {
      ...formData,
      amount: parseFloat(formData.amount),
      dueDayOfMonth: formData.dueDayOfMonth ? parseInt(formData.dueDayOfMonth) : null,
      orderIndex: parseInt(formData.orderIndex),
    }

    if (selectedFee) {
      updateMutation.mutate({ id: selectedFee.id, data: submitData })
    } else {
      createMutation.mutate(submitData)
    }
  }

  const columns = [
    { field: 'name', headerName: 'Fee Name' },
    { field: 'program', headerName: 'Program' },
    { field: 'semester', headerName: 'Semester' },
    { field: 'academicYear', headerName: 'Academic Year' },
    {
      field: 'amount',
      headerName: 'Amount',
      render: (value) => `$${parseFloat(value || 0).toFixed(2)}`,
    },
    {
      field: 'isActive',
      headerName: 'Active',
      render: (value) => (
        <Chip label={value ? 'Yes' : 'No'} color={value ? 'success' : 'default'} size="small" />
      ),
    },
    {
      field: 'isRequired',
      headerName: 'Required',
      render: (value) => (
        <Chip label={value ? 'Yes' : 'No'} color={value ? 'info' : 'default'} size="small" />
      ),
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
    return <Alert severity="error">Failed to load fee structures</Alert>
  }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Fee Structures</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => {
            setSelectedFee(null)
            resetForm()
            setOpenDialog(true)
          }}
        >
          Add Fee Structure
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
        emptyMessage="No fee structures found"
      />

      <FormDialog
        open={openDialog}
        title={selectedFee ? 'Edit Fee Structure' : 'Add Fee Structure'}
        onClose={() => {
          setOpenDialog(false)
          setSelectedFee(null)
          resetForm()
        }}
        onSubmit={handleSubmit}
        loading={createMutation.isLoading || updateMutation.isLoading}
      >
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Program"
              value={formData.program}
              onChange={(e) => setFormData({ ...formData, program: e.target.value })}
              required
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Semester"
              value={formData.semester}
              onChange={(e) => setFormData({ ...formData, semester: e.target.value })}
              required
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Academic Year"
              value={formData.academicYear}
              onChange={(e) => setFormData({ ...formData, academicYear: e.target.value })}
              required
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Fee Name"
              value={formData.name}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
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
              rows={2}
            />
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
              label="Due Day of Month"
              type="number"
              value={formData.dueDayOfMonth}
              onChange={(e) => setFormData({ ...formData, dueDayOfMonth: e.target.value })}
              inputProps={{ min: 1, max: 31 }}
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <FormControlLabel
              control={
                <Switch
                  checked={formData.isActive}
                  onChange={(e) => setFormData({ ...formData, isActive: e.target.checked })}
                />
              }
              label="Active"
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <FormControlLabel
              control={
                <Switch
                  checked={formData.isRequired}
                  onChange={(e) => setFormData({ ...formData, isRequired: e.target.checked })}
                />
              }
              label="Required"
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <FormControlLabel
              control={
                <Switch
                  checked={formData.recurring}
                  onChange={(e) => setFormData({ ...formData, recurring: e.target.checked })}
                />
              }
              label="Recurring"
            />
          </Grid>
        </Grid>
      </FormDialog>

      <ConfirmDialog
        open={openDeleteDialog}
        title="Delete Fee Structure"
        description={`Are you sure you want to delete "${selectedFee?.name}"?`}
        onClose={() => {
          setOpenDeleteDialog(false)
          setSelectedFee(null)
        }}
        onConfirm={() => deleteMutation.mutate(selectedFee?.id)}
        confirmText="Delete"
      />
    </Box>
  )
}

export default FeeStructures
