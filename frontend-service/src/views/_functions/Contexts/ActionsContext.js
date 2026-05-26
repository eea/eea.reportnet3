import { createContext } from 'react';

export const ActionsContext = createContext({
  changeExportDatasetState: isLoading => {},
  changeExportTableState: isTableLoading => {},
  deleteDatasetProcessing: false,
  deleteDatasetCode: null,
  deleteTableProcessing: false,
  exportDatasetProcessing: false,
  exportTableProcessing: false,
  importDatasetProcessing: false,
  importTableProcessing: false,
  isInProgress: false,
  testProcess: (datasetId, action, code) => {},
  validateDatasetProcessing: false,
});
