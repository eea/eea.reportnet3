import { useState, useRef } from 'react';

import { ActionsContext } from '../Contexts/ActionsContext';

import { JobsStatusesService } from 'services/JobsStatusesService';

export const ActionsProvider = ({ children }) => {
  const [deleteDatasetProcessing, setDeleteDatasetProcessing] = useState(false);
  const [deleteTableProcessing, setDeleteTableProcessing] = useState(false);
  const [exportDatasetProcessing, setExportDatasetProcessing] = useState(false);
  const [exportTableProcessing, setExportTableProcessing] = useState(false);
  const [importDatasetProcessing, setImportDatasetProcessing] = useState(false);
  const [importTableProcessing, setImportTableProcessing] = useState(false);
  const [isInProgress, setIsInProgress] = useState(false);
  const [jobTypeInProgress, setJobTypeInProgress] = useState('');
  const [validateDatasetProcessing, setValidateDatasetProcessing] = useState(false);

  const inProgressRef = useRef();
  const jobTypeRef = useRef();

  inProgressRef.current = isInProgress;
  jobTypeRef.current = jobTypeInProgress;

  const testProcess = (datasetId, action) => {
    setJobTypeInProgress('');

    const timeout = ms => {
      return new Promise(resolve => setTimeout(resolve, ms));
    };

    let pageRefresh = false;

    const testProcessTimer = async () => {
      setIsInProgress(false);

      const datasetJobs = await JobsStatusesService.getJobsStatuses({
        datasetId: datasetId,
        numberRows: 1000
      });

      for (let i = datasetJobs.jobsList.length - 1; i >= 0; i--) {
        if (
          (datasetJobs.jobsList[i].jobStatus === 'IN_PROGRESS' || datasetJobs.jobsList[i].jobStatus === 'QUEUED') &&
          (datasetJobs.jobsList[i].jobType === 'IMPORT' || datasetJobs.jobsList[i].jobType === 'VALIDATION')
        ) {
          setIsInProgress(true);
          setJobTypeInProgress(datasetJobs.jobsList[i].jobType);
          break;
        }
      }

      if (!pageRefresh && !action && jobTypeRef.current === 'IMPORT') {
        pageRefresh = true;
        actionInProgress('IMPORT');
      } else if (!pageRefresh && !action && jobTypeRef.current === 'VALIDATION') {
        pageRefresh = true;
        actionInProgress('VALIDATION');
      }

      if (!inProgressRef.current) {
        switch (action) {
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
            if (jobTypeRef.current === 'IMPORT') {
              setImportDatasetProcessing(false);
            } else {
              setValidateDatasetProcessing(false);
            }
        }
      } else {
        await timeout(5000);
        testProcessTimer();
      }
    };

    const actionInProgress = async action => {
      switch (action) {
        case 'IMPORT':
          setImportDatasetProcessing(true);
          break;
        case 'VALIDATION':
          setValidateDatasetProcessing(true);
          break;
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
          await timeout(5000);
      }

      if (!(action === 'IMPORT' || action === 'VALIDATION')) {
        testProcessTimer();
      }
    };

    if (!action) {
      testProcessTimer();
    } else {
      actionInProgress(action);
    }
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
