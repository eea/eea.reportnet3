import { useState } from 'react';

import { ActionsContext } from '../Contexts/ActionsContext';

import { DatasetService } from 'services/DatasetService';

export const ActionsProvider = ({ children }) => {
  const [importDatasetProcessing, setImportDatasetProcessing] = useState(false);
  const [importTableProcessing, setImportTableProcessing] = useState(false);
  const [exportDatasetProcessing, setExportDatasetProcessing] = useState(false);
  const [exportTableProcessing, setExportTableProcessing] = useState(false);
  const [deleteDatasetProcessing, setDeleteDatasetProcessing] = useState(false);
  const [deleteTableProcessing, setDeleteTableProcessing] = useState(false);
  const [validateDatasetProcessing, setValidateDatasetProcessing] = useState(false);

  const testProcess = (datasetId, testCase) => {
    const timeout = ms => {
      return new Promise(resolve => setTimeout(resolve, ms));
    };
    const testProcessTimer = async () => {
      const processStatus = await DatasetService.testImportProcess(datasetId);

      if (processStatus?.data && !processStatus?.data?.importInProgress) {
        switch (testCase) {
          case 'DATASET_IMPORT':
            setImportDatasetProcessing(false);
            break;
          case 'TABLE_IMPORT':
            setImportTableProcessing(false);
            break;
          case 'DATASET_EXPORT':
            setExportDatasetProcessing(false);
            break;
          case 'TABLE_EXPORT':
            setExportTableProcessing(false);
            break;
          case 'DATASET_DELETE':
            setDeleteDatasetProcessing(false);
            break;
          case 'TABLE_DELETE':
            setDeleteTableProcessing(false);
            break;
          default:
            setValidateDatasetProcessing(false);
        }
      } else {
        await timeout(5000);
        testProcessTimer();
      }
    };
    switch (testCase) {
      case 'DATASET_IMPORT':
        setImportDatasetProcessing(true);
        break;
      case 'TABLE_IMPORT':
        setImportTableProcessing(true);
        break;
      case 'DATASET_EXPORT':
        setExportDatasetProcessing(true);
        break;
      case 'TABLE_EXPORT':
        setExportTableProcessing(true);
        break;
      case 'DATASET_DELETE':
        setDeleteDatasetProcessing(true);
        break;
      case 'TABLE_DELETE':
        setDeleteTableProcessing(true);
        break;
      default:
        setValidateDatasetProcessing(true);
    }
    testProcessTimer();
  };

  const changeExportDatasetState = isLoading => {
    setExportDatasetProcessing(isLoading);
  };

  const changeExportTableState = isTableLoading => {
    setExportTableProcessing(isTableLoading);
  };

  return (
    <ActionsContext.Provider
      value={{
        importDatasetProcessing: importDatasetProcessing,
        importTableProcessing: importTableProcessing,
        exportDatasetProcessing: exportDatasetProcessing,
        exportTableProcessing: exportTableProcessing,
        deleteDatasetProcessing: deleteDatasetProcessing,
        deleteTableProcessing: deleteTableProcessing,
        validateDatasetProcessing: validateDatasetProcessing,
        changeExportDatasetState: changeExportDatasetState,
        changeExportTableState: changeExportTableState,
        testProcess: testProcess
      }}>
      {children}
    </ActionsContext.Provider>
  );
};
