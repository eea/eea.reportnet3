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
  const timer = useRef();

  inProgressRef.current = isInProgress;
  jobTypeRef.current = jobTypeInProgress;

  const testProcess = (datasetId, action) => {
    clearInterval(timer.current);

    setImportDatasetProcessing(false);
    setImportTableProcessing(false);
    setValidateDatasetProcessing(false);
    setJobTypeInProgress('');
    setIsInProgress(false);

    let pageRefresh = false;

    switch (action) {
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
      case 'DATASET_VALIDATE':
        setValidateDatasetProcessing(true);
        break;
      default:
        break;
    }

    timer.current = setInterval(async () => {

      const datasetJobs = await JobsStatusesService.getJobsStatuses({
        datasetId: datasetId,
        numberRows: 1000
      });

      let lastJob = datasetJobs.jobsList[datasetJobs.jobsList.length - 1]
      let lastJobStatus = lastJob.jobStatus;
      
      if( lastJobStatus === 'FINISHED' || lastJobStatus === 'REFUSED' || lastJobStatus === 'FAILED' || lastJobStatus === 'CANCELED'){
        setIsInProgress(false);
        clearInterval(timer.current);
        setJobTypeInProgress(lastJob.jobType)
      }else setIsInProgress(true)

      if (!pageRefresh && !action && jobTypeRef.current === 'IMPORT') {
        pageRefresh = true;
        setImportDatasetProcessing(true);
      } else if (!pageRefresh && !action && jobTypeRef.current === 'VALIDATION') {
        pageRefresh = true;
        setValidateDatasetProcessing(true);
      }

      if (!inProgressRef.current) {
        switch (action) {
          case 'DATASET_IMPORT':
            setImportDatasetProcessing(false);
            clearInterval(timer.current);
            break;
          case 'TABLE_IMPORT':
            setImportTableProcessing(false);
            clearInterval(timer.current);
            break;
          case 'DATASET_EXPORT':
            setExportDatasetProcessing(false);
            clearInterval(timer.current);
            break;
          case 'TABLE_EXPORT':
            setExportTableProcessing(false);
            clearInterval(timer.current);
            break;
          case 'DATASET_DELETE':
            setDeleteDatasetProcessing(false);
            clearInterval(timer.current);
            break;
          case 'TABLE_DELETE':
            setDeleteTableProcessing(false);
            clearInterval(timer.current);
            break;
          default:
            if (jobTypeRef.current === 'IMPORT') {
              setImportDatasetProcessing(false);
              clearInterval(timer.current);
            } else {
              setValidateDatasetProcessing(false);
              clearInterval(timer.current);
            }
        }
      }
    }, 1000);
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
