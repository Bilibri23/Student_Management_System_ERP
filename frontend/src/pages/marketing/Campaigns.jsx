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
import { marketingApi } from '../../api/marketing'
import DataTable from '../../components/common/DataTable'
import FormDialog from '../../components/common/FormDialog'
import ConfirmDialog from '../../components/common/ConfirmDialog'
import { useAuthStore } from '../../store/authStore'

const Campaigns = () => {
  const { user } = useAuthStore()
  const queryClient = useQueryClient()
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(10)
  const [openDialog, setOpenDialog] = useState(false)
  const [openDeleteDialog, setOpenDeleteDialog] = useState(false)
  const [selectedCampaign, setSelectedCampaign] = useState(null)
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    type: 'ONLINE',
    status: 'PLANNED',
    startDate: new Date().toISOString().split('T')[0],
    endDate: '',
    budget: '',
    targetAudience: '',
    expectedLeads: '',
    expectedEnrollments: '',
    managerId: user?.id || '',
    channel: '',
    notes: '',
  })

  const { data, isLoading, error } = useQuery({
    queryKey: ['campaigns', page, pageSize],
    queryFn: () =>
      marketingApi.getCampaigns({ page, size: pageSize }).then(res => res.data.data),
  })

  const createMutation = useMutation({
    mutationFn: (data) => marketingApi.createCampaign(data),
    onSuccess: () => {
      queryClient.invalidateQueries(['campaigns'])
      setOpenDialog(false)
      resetForm()
    },
  })

  const updateMutation = useMutation({
    mutationFn: ({ id, data }) => marketingApi.updateCampaign(id, data),
    onSuccess: () => {
      queryClient.invalidateQueries(['campaigns'])
      setOpenDialog(false)
      setSelectedCampaign(null)
      resetForm()
    },
  })

  const deleteMutation = useMutation({
    mutationFn: (id) => marketingApi.deleteCampaign(id),
    onSuccess: () => {
      queryClient.invalidateQueries(['campaigns'])
      setOpenDeleteDialog(false)
      setSelectedCampaign(null)
    },
  })

  const resetForm = () => {
    setFormData({
      name: '',
      description: '',
      type: 'ONLINE',
      status: 'PLANNED',
      startDate: new Date().toISOString().split('T')[0],
      endDate: '',
      budget: '',
      targetAudience: '',
      expectedLeads: '',
      expectedEnrollments: '',
      managerId: user?.id || '',
      channel: '',
      notes: '',
    })
  }

  const handleEdit = (campaign) => {
    setSelectedCampaign(campaign)
    setFormData({
      name: campaign.name || '',
      description: campaign.description || '',
      type: campaign.type || 'ONLINE',
      status: campaign.status || 'PLANNED',
      startDate: campaign.startDate
        ? new Date(campaign.startDate).toISOString().split('T')[0]
        : new Date().toISOString().split('T')[0],
      endDate: campaign.endDate
        ? new Date(campaign.endDate).toISOString().split('T')[0]
        : '',
      budget: campaign.budget || '',
      targetAudience: campaign.targetAudience || '',
      expectedLeads: campaign.expectedLeads || '',
      expectedEnrollments: campaign.expectedEnrollments || '',
      managerId: campaign.managerId || user?.id || '',
      channel: campaign.channel || '',
      notes: campaign.notes || '',
    })
    setOpenDialog(true)
  }

  const handleDelete = (campaign) => {
    setSelectedCampaign(campaign)
    setOpenDeleteDialog(true)
  }

  const handleSubmit = () => {
    const submitData = {
      ...formData,
      budget: parseFloat(formData.budget),
      expectedLeads: formData.expectedLeads ? parseInt(formData.expectedLeads) : null,
      expectedEnrollments: formData.expectedEnrollments
        ? parseInt(formData.expectedEnrollments)
        : null,
      managerId: parseInt(formData.managerId),
    }

    if (selectedCampaign) {
      updateMutation.mutate({ id: selectedCampaign.id, data: submitData })
    } else {
      createMutation.mutate(submitData)
    }
  }

  const getStatusColor = (status) => {
    switch (status) {
      case 'ACTIVE':
        return 'success'
      case 'PLANNED':
        return 'info'
      case 'COMPLETED':
        return 'default'
      case 'CANCELLED':
        return 'error'
      default:
        return 'default'
    }
  }

  const columns = [
    { field: 'name', headerName: 'Campaign Name' },
    {
      field: 'type',
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
      field: 'budget',
      headerName: 'Budget',
      render: (value) => `$${parseFloat(value || 0).toFixed(2)}`,
    },
    {
      field: 'spentAmount',
      headerName: 'Spent',
      render: (value) => `$${parseFloat(value || 0).toFixed(2)}`,
    },
    {
      field: 'actualLeads',
      headerName: 'Leads',
      render: (_, row) => `${row.actualLeads || 0} / ${row.expectedLeads || 0}`,
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
    return <Alert severity="error">Failed to load campaigns</Alert>
  }

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4">Campaigns</Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => {
            setSelectedCampaign(null)
            resetForm()
            setOpenDialog(true)
          }}
        >
          Add Campaign
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
        emptyMessage="No campaigns found"
      />

      <FormDialog
        open={openDialog}
        title={selectedCampaign ? 'Edit Campaign' : 'Add Campaign'}
        onClose={() => {
          setOpenDialog(false)
          setSelectedCampaign(null)
          resetForm()
        }}
        onSubmit={handleSubmit}
        loading={createMutation.isLoading || updateMutation.isLoading}
      >
        <Grid container spacing={2}>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Campaign Name"
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
              rows={3}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              select
              label="Type"
              value={formData.type}
              onChange={(e) => setFormData({ ...formData, type: e.target.value })}
              required
            >
              <MenuItem value="ONLINE">Online</MenuItem>
              <MenuItem value="OFFLINE">Offline</MenuItem>
              <MenuItem value="SOCIAL_MEDIA">Social Media</MenuItem>
              <MenuItem value="EMAIL">Email</MenuItem>
              <MenuItem value="EVENT">Event</MenuItem>
            </TextField>
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              select
              label="Status"
              value={formData.status}
              onChange={(e) => setFormData({ ...formData, status: e.target.value })}
            >
              <MenuItem value="PLANNED">Planned</MenuItem>
              <MenuItem value="ACTIVE">Active</MenuItem>
              <MenuItem value="COMPLETED">Completed</MenuItem>
              <MenuItem value="CANCELLED">Cancelled</MenuItem>
            </TextField>
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
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Budget"
              type="number"
              value={formData.budget}
              onChange={(e) => setFormData({ ...formData, budget: e.target.value })}
              required
              inputProps={{ step: '0.01' }}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Channel"
              value={formData.channel}
              onChange={(e) => setFormData({ ...formData, channel: e.target.value })}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Expected Leads"
              type="number"
              value={formData.expectedLeads}
              onChange={(e) => setFormData({ ...formData, expectedLeads: e.target.value })}
            />
          </Grid>
          <Grid item xs={12} sm={6}>
            <TextField
              fullWidth
              label="Expected Enrollments"
              type="number"
              value={formData.expectedEnrollments}
              onChange={(e) => setFormData({ ...formData, expectedEnrollments: e.target.value })}
            />
          </Grid>
          <Grid item xs={12}>
            <TextField
              fullWidth
              label="Target Audience"
              value={formData.targetAudience}
              onChange={(e) => setFormData({ ...formData, targetAudience: e.target.value })}
              multiline
              rows={2}
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
        </Grid>
      </FormDialog>

      <ConfirmDialog
        open={openDeleteDialog}
        title="Delete Campaign"
        description={`Are you sure you want to delete "${selectedCampaign?.name}"?`}
        onClose={() => {
          setOpenDeleteDialog(false)
          setSelectedCampaign(null)
        }}
        onConfirm={() => deleteMutation.mutate(selectedCampaign?.id)}
        confirmText="Delete"
      />
    </Box>
  )
}

export default Campaigns
