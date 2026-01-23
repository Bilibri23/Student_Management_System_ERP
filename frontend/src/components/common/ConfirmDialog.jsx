import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Button,
  Box,
  Typography,
  IconButton,
  CircularProgress,
} from '@mui/material'
import { Warning as WarningIcon, Close as CloseIcon } from '@mui/icons-material'

const ConfirmDialog = ({ open, title, description, onClose, onConfirm, confirmText = 'Confirm', loading = false }) => {
  return (
    <Dialog
      open={open}
      onClose={loading ? undefined : onClose}
      maxWidth="xs"
      fullWidth
      PaperProps={{
        sx: {
          borderRadius: 4,
          boxShadow: '0 25px 50px -12px rgba(0, 0, 0, 0.25)',
        },
      }}
    >
      <DialogTitle
        sx={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          pb: 1,
        }}
      >
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 1.5 }}>
          <Box
            sx={{
              width: 40,
              height: 40,
              borderRadius: 2,
              bgcolor: '#FEF2F2',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <WarningIcon sx={{ color: '#EF4444' }} />
          </Box>
          <Typography variant="h6" fontWeight={600}>
            {title || 'Confirm Action'}
          </Typography>
        </Box>
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
      {description && (
        <DialogContent sx={{ pt: 2 }}>
          <DialogContentText sx={{ color: 'text.secondary' }}>
            {description}
          </DialogContentText>
        </DialogContent>
      )}
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
          onClick={onConfirm}
          disabled={loading}
          variant="contained"
          sx={{
            minWidth: 100,
            background: 'linear-gradient(135deg, #EF4444 0%, #F87171 100%)',
            boxShadow: '0 4px 14px 0 rgba(239, 68, 68, 0.39)',
            '&:hover': {
              background: 'linear-gradient(135deg, #DC2626 0%, #EF4444 100%)',
              boxShadow: '0 6px 20px rgba(239, 68, 68, 0.45)',
            },
            '&:disabled': {
              background: 'linear-gradient(135deg, #94A3B8 0%, #CBD5E1 100%)',
            },
          }}
        >
          {loading ? <CircularProgress size={20} sx={{ color: 'white' }} /> : confirmText}
        </Button>
      </DialogActions>
    </Dialog>
  )
}

export default ConfirmDialog


