import { createContext } from 'react';

export const ActionsContext = createContext({
  changeExportDatasetState: isLoading => {},
  changeExportTableState: isTableLoading => {},
  deleteDatasetProcessing: false,
  deleteTableProcessing: false,
  exportDatasetProcessing: false,
  exportTableProcessing: false,
  importDatasetProcessing: false,
  importTableProcessing: false,
  isInProgress: false,
  testProcess: (datasetId, action) => {},
  validateDatasetProcessing: false,
});
