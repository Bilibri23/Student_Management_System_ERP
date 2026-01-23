import { Box, Typography, Button, Chip } from '@mui/material'
import { Add as AddIcon } from '@mui/icons-material'

const PageHeader = ({
  title,
  subtitle,
  action,
  actionLabel = 'Add New',
  actionIcon = <AddIcon />,
  badge,
  children,
}) => {
  return (
    <Box
      sx={{
        display: 'flex',
        flexDirection: { xs: 'column', sm: 'row' },
        justifyContent: 'space-between',
        alignItems: { xs: 'flex-start', sm: 'center' },
        gap: 2,
        mb: 4,
        pb: 3,
        borderBottom: '1px solid #E2E8F0',
      }}
    >
      <Box>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Typography
            variant="h4"
            fontWeight={700}
            sx={{
              background: 'linear-gradient(135deg, #1E293B 0%, #475569 100%)',
              WebkitBackgroundClip: 'text',
              WebkitTextFillColor: 'transparent',
            }}
          >
            {title}
          </Typography>
          {badge && (
            <Chip
              label={badge}
              size="small"
              sx={{
                bgcolor: '#6366F115',
                color: '#6366F1',
                fontWeight: 600,
                fontSize: '0.7rem',
              }}
            />
          )}
        </Box>
        {subtitle && (
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
            {subtitle}
          </Typography>
        )}
      </Box>
      <Box sx={{ display: 'flex', gap: 2, alignItems: 'center' }}>
        {children}
        {action && (
          <Button
            variant="contained"
            startIcon={actionIcon}
            onClick={action}
            sx={{
              background: 'linear-gradient(135deg, #6366F1 0%, #8B5CF6 100%)',
              boxShadow: '0 4px 14px 0 rgba(99, 102, 241, 0.39)',
              '&:hover': {
                background: 'linear-gradient(135deg, #4F46E5 0%, #7C3AED 100%)',
                boxShadow: '0 6px 20px rgba(99, 102, 241, 0.45)',
              },
            }}
          >
            {actionLabel}
          </Button>
        )}
      </Box>
    </Box>
  )
}

export default PageHeader
