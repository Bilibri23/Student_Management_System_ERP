import { Button } from '@mui/material'
import { Download as DownloadIcon } from '@mui/icons-material'
import { exportToExcel } from '../../utils/excelExport'

const ExportButton = ({ data, filename, sheetName = 'Sheet1', disabled = false, ...props }) => {
  const handleExport = () => {
    if (!data || data.length === 0) {
      alert('No data to export')
      return
    }
    exportToExcel(data, filename, sheetName)
  }

  return (
    <Button
      variant="outlined"
      startIcon={<DownloadIcon />}
      onClick={handleExport}
      disabled={disabled || !data || data.length === 0}
      {...props}
    >
      Export to Excel
    </Button>
  )
}

export default ExportButton

