import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  IconButton,
  Typography,
  CircularProgress,
} from '@mui/material'
import { Close as CloseIcon } from '@mui/icons-material'

const FormDialog = ({ open, title, children, onClose, onSubmit, submitText = 'Save', loading = false, maxWidth = 'sm' }) => {
  const handleSubmit = (e) => {
    e.preventDefault()
    onSubmit?.()
  }

  return (
    <Dialog
      open={open}
      onClose={loading ? undefined : onClose}
      maxWidth={maxWidth}
      fullWidth
      PaperProps={{
        sx: {
          borderRadius: 4,
          boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
        },
      }}
    >
      {title && (
        <DialogTitle
          sx={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            pb: 1,
            borderBottom: '1px solid #E2E8F0',
          }}
        >
          <Typography variant="h6" fontWeight={600}>
            {title}
          </Typography>
          <IconButton
            onClick={onClose}
            disabled={loading}
            size="small"
            sx={{
              color: 'text.secondary',
              '&:hover': {
                bgcolor: '#FEF2F2',
                color: '#EF4444',
              },
            }}
          >
            <CloseIcon fontSize="small" />
          </IconButton>
        </DialogTitle>
      )}
      <Box component="form" onSubmit={handleSubmit}>
        <DialogContent sx={{ pt: 3 }}>{children}</DialogContent>
        <DialogActions
          sx={{
            px: 3,
            py: 2,
            borderTop: '1px solid #E2E8F0',
            gap: 1,
          }}
        >
          <Button
            onClick={onClose}
            disabled={loading}
            variant="outlined"
            sx={{
              borderColor: '#E2E8F0',
              color: '#64748B',
              '&:hover': {
                borderColor: '#CBD5E1',
                bgcolor: '#F8FAFC',
              },
            }}
          >
            Cancel
          </Button>
          <Button
            type="submit"
            variant="contained"
            disabled={loading}
            sx={{
              minWidth: 100,
              background: 'linear-gradient(135deg, #6366F1 0%, #8B5CF6 100%)',
              boxShadow: '0 4px 14px 0 rgba(99, 102, 241, 0.39)',
              '&:hover': {
                background: 'linear-gradient(135deg, #4F46E5 0%, #7C3AED 100%)',
                boxShadow: '0 6px 20px rgba(99, 102, 241, 0.45)',
              },
              '&:disabled': {
                background: 'linear-gradient(135deg, #94A3B8 0%, #CBD5E1 100%)',
              },
            }}
          >
            {loading ? <CircularProgress size={20} sx={{ color: 'white' }} /> : submitText}
          </Button>
        </DialogActions>
      </Box>
    </Dialog>
  )
}

export default FormDialog


