import { useState, useContext, useEffect, useReducer } from 'react';

import { useSnapshotReducer } from './useSnapshotReducer';

import { SnapshotService } from 'services/SnapshotService';
import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';

const useReporterDataset = (datasetId, dataflowId) => {
  const notificationContext = useContext(NotificationContext);

  const [isLoadingSnapshotListData, setIsLoadingSnapshotListData] = useState(true);
  const [isSnapshotsBarVisible, setIsSnapshotsBarVisible] = useState(false);
  const [isSnapshotDialogVisible, setIsSnapshotDialogVisible] = useState(false);
  const [snapshotListData, setSnapshotListData] = useState([]);

  const snapshotInitialState = {
    action: () => {},
    apiCall: '',
    createdAt: '',
    dataflowId,
    datasetId,
    description: '',
    dialogConfirmMessage: '',
    dialogConfirmQuestion: '',
    dialogMessage: '',
    isConfirmDisabled: false,
    snapShotId: ''
  };

  useEffect(() => {
    if (isSnapshotsBarVisible) {
      onLoadSnapshotList();
    }
  }, [isSnapshotsBarVisible]);

  const onCreateSnapshot = async () => {
    try {
      await SnapshotService.createReporter(datasetId, snapshotState.description);
      snapshotDispatch({ type: 'ON_SNAPSHOT_RESET' });
      onLoadSnapshotList();
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' });
      } else {
        console.error('useReporterDataset - onCreateSnapshot.', error);
        notificationContext.add({ type: 'CREATE_BY_ID_REPORTER_ERROR', content: {} });
      }
    } finally {
      setIsSnapshotDialogVisible(false);
    }
  };

  const onDeleteSnapshot = async () => {
    try {
      await SnapshotService.deleteReporter(datasetId, snapshotState.snapShotId);
      onLoadSnapshotList();
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' });
      } else {
        console.error('useReporterDataset - onDeleteSnapshot.', error);
        notificationContext.add({ type: 'DELETED_BY_ID_REPORTER_ERROR', content: {} });
      }
    } finally {
      setIsSnapshotDialogVisible(false);
    }
  };

  const onLoadSnapshotList = async () => {
    try {
      setIsLoadingSnapshotListData(true);

      //SetTimeout for avoiding the overlapping between the slidebar transition and the api call
      setTimeout(async () => {
        const snapshotsData = await SnapshotService.getAllReporter(datasetId);
        setSnapshotListData(snapshotsData);
        setIsLoadingSnapshotListData(false);
      }, 500);
    } catch (error) {
      console.error('useReporterDataset - onLoadSnapshotList.', error);
      notificationContext.add({ type: 'ALL_REPORTER_ERROR', content: {} });
      setIsLoadingSnapshotListData(false);
    }
  };

  const onRestoreSnapshot = async () => {
    try {
      notificationContext.add({ type: 'RESTORE_DATASET_SNAPSHOT_INIT_INFO' });
      await SnapshotService.restoreReporter(dataflowId, datasetId, snapshotState.snapShotId);
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' });
      } else {
        console.error('useReporterDataset - onRestoreSnapshot.', error);
        notificationContext.add({ type: 'RESTORE_DATASET_SNAPSHOT_FAILED_EVENT', content: {} });
      }
    } finally {
      setIsSnapshotDialogVisible(false);
    }
  };

  const { snapshotReducer } = useSnapshotReducer(
    setIsSnapshotDialogVisible,
    onCreateSnapshot,
    onDeleteSnapshot,
    onRestoreSnapshot
  );

  const [snapshotState, snapshotDispatch] = useReducer(snapshotReducer, snapshotInitialState);

  return {
    isLoadingSnapshotListData,
    isSnapshotDialogVisible,
    isSnapshotsBarVisible,
    setIsSnapshotDialogVisible,
    setIsSnapshotsBarVisible,
    snapshotDispatch,
    snapshotListData,
    snapshotState
  };
};

export { useReporterDataset };
