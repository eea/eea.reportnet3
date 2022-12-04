import { createContext } from 'react';

export const ActionsContext = createContext({
  importDatasetProcessing: false,
  importTableProcessing: false,
  exportDatasetProcessing: false,
  exportTableProcessing: false,
  deleteDatasetProcessing: false,
  deleteTableProcessing: false,
  validateDatasetProcessing: false,
  onLandTestProcess: false,
  changeExportDatasetState: isLoading => {},
  changeExportTableState: isTableLoading => {},
  testProcess: (datasetId, testCase) => {}
});
