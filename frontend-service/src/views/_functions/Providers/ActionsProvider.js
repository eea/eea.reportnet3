import { useState, useRef } from 'react';

import { ActionsContext } from '../Contexts/ActionsContext';

import { JobsStatusesService } from 'services/JobsStatusesService';
import { isEmpty } from 'lodash';

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

    setDeleteDatasetProcessing(false);
    setDeleteTableProcessing(false);
    setImportDatasetProcessing(false);
    setImportTableProcessing(false);
    setValidateDatasetProcessing(false);
    setJobTypeInProgress('');
    setIsInProgress(action ? true : false);

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
      const jobsInProgress = await JobsStatusesService.getJobsStatuses({
        datasetId: datasetId,
        jobStatus: ['QUEUED', 'IN_PROGRESS'].join()
      });

      if (isEmpty(jobsInProgress.jobsList)) {
        setIsInProgress(false);
        clearInterval(timer.current);
      } else {
        setIsInProgress(true);
        const jobInProgress = jobsInProgress.jobsList.find(job => job.jobStatus === 'IN_PROGRESS');
        setJobTypeInProgress(jobInProgress.jobType);
      }

      if (!pageRefresh && !action) {
        if (jobTypeRef.current === 'IMPORT') {
          pageRefresh = true;
          setImportDatasetProcessing(true);
        } else if (jobTypeRef.current === 'VALIDATION') {
          pageRefresh = true;
          setValidateDatasetProcessing(true);
        } else if (jobTypeRef.current === 'DELETE') {
          pageRefresh = true;
          setDeleteDatasetProcessing(true);
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
        changeExportDatasetState,
        changeExportTableState,
        deleteDatasetProcessing,
        deleteTableProcessing,
        exportDatasetProcessing,
        exportTableProcessing,
        importDatasetProcessing,
        importTableProcessing,
        isInProgress,
        testProcess,
        validateDatasetProcessing
      }}>
      {children}
    </ActionsContext.Provider>
  );
};
