import { useState, useContext, useEffect, useReducer } from 'react';

import { useSnapshotReducer } from './useSnapshotReducer';

import { SnapshotService } from 'core/services/Snapshot';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

const useReporterDataset = (datasetId, dataflowId) => {
  const notificationContext = useContext(NotificationContext);

  const [isLoadingSnapshotListData, setIsLoadingSnapshotListData] = useState(true);
  const [isSnapshotsBarVisible, setIsSnapshotsBarVisible] = useState(false);
  const [isSnapshotDialogVisible, setIsSnapshotDialogVisible] = useState(false);
  const [snapshotListData, setSnapshotListData] = useState([]);

  const snapshotInitialState = {
    apiCall: '',
    createdAt: '',
    description: '',
    dialogMessage: '',
    dataflowId,
    datasetId,
    snapShotId: '',
    action: () => {}
  };

  useEffect(() => {
    if (isSnapshotsBarVisible) {
      onLoadSnapshotList();
    }
  }, [isSnapshotsBarVisible]);

  const onCreateSnapshot = async () => {
    try {
      await SnapshotService.createByIdReporter(datasetId, snapshotState.description);
      onLoadSnapshotList();
    } catch (error) {
      notificationContext.add({
        type: 'CREATE_BY_ID_REPORTER_ERROR',
        content: {}
      });
    } finally {
      setIsSnapshotDialogVisible(false);
    }
  };

  const onDeleteSnapshot = async () => {
    try {
      await SnapshotService.deleteByIdReporter(datasetId, snapshotState.snapShotId);
      onLoadSnapshotList();
    } catch (error) {
      notificationContext.add({
        type: 'DELETED_BY_ID_REPORTER_ERROR',
        content: {}
      });
    } finally {
      setIsSnapshotDialogVisible(false);
    }
  };

  const onLoadSnapshotList = async () => {
    try {
      setIsLoadingSnapshotListData(true);

      //Settimeout for avoiding the overlaping between the slidebar transition and the api call
      setTimeout(async () => {
        const snapshotsData = await SnapshotService.allReporter(datasetId);

        setSnapshotListData(snapshotsData);

        setIsLoadingSnapshotListData(false);
      }, 500);
    } catch (error) {
      notificationContext.add({
        type: 'ALL_REPORTER_ERROR',
        content: {}
      });
      setIsLoadingSnapshotListData(false);
    }
  };

  const onReleaseSnapshot = async () => {
    try {
      await SnapshotService.releaseByIdReporter(dataflowId, datasetId, snapshotState.snapShotId);
      onLoadSnapshotList();
    } catch (error) {
      notificationContext.add({
        type: 'RELEASED_BY_ID_REPORTER_ERROR',
        content: {}
      });
    } finally {
      setIsSnapshotDialogVisible(false);
    }
  };

  const onRestoreSnapshot = async () => {
    try {
      await SnapshotService.restoreByIdReporter(dataflowId, datasetId, snapshotState.snapShotId);
      snapshotDispatch({ type: 'mark_as_restored', payload: {} });
    } catch (error) {
      notificationContext.add({
        type: 'RESTORED_BY_ID_REPORTER_ERROR',
        content: {}
      });
    } finally {
      setIsSnapshotDialogVisible(false);
    }
  };

  const { snapshotReducer } = useSnapshotReducer(
    setIsSnapshotDialogVisible,
    onCreateSnapshot,
    onDeleteSnapshot,
    onRestoreSnapshot,
    onReleaseSnapshot
  );

  const [snapshotState, snapshotDispatch] = useReducer(snapshotReducer, snapshotInitialState);

  return {
    isLoadingSnapshotListData,
    isSnapshotsBarVisible,
    setIsSnapshotsBarVisible,
    isSnapshotDialogVisible,
    setIsSnapshotDialogVisible,
    snapshotDispatch,
    snapshotListData,
    snapshotState
  };
};

export { useReporterDataset };
