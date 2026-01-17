import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
} from '@mui/material'

const FormDialog = ({ open, title, children, onClose, onSubmit, submitText = 'Save', loading = false }) => {
  const handleSubmit = (e) => {
    e.preventDefault()
    onSubmit?.()
  }

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      {title && <DialogTitle>{title}</DialogTitle>}
      <Box component="form" onSubmit={handleSubmit}>
        <DialogContent>{children}</DialogContent>
        <DialogActions>
          <Button onClick={onClose} disabled={loading}>
            Cancel
          </Button>
          <Button type="submit" variant="contained" disabled={loading}>
            {submitText}
          </Button>
        </DialogActions>
      </Box>
    </Dialog>
  )
}

export default FormDialog


