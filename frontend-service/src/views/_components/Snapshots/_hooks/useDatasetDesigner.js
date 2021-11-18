import { useState, useContext, useEffect, useReducer } from 'react';

import { useSnapshotReducer } from './useSnapshotReducer';
import { SnapshotService } from 'services/SnapshotService';
import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';

import { useCheckNotifications } from 'views/_functions/Hooks/useCheckNotifications';

const useDatasetDesigner = (dataflowId, datasetId, datasetSchemaId) => {
  const notificationContext = useContext(NotificationContext);
  const [isLoadingSnapshotListData, setIsLoadingSnapshotListData] = useState(false);
  const [isSnapshotsBarVisible, setIsSnapshotsBarVisible] = useState(false);
  const [isSnapshotDialogVisible, setIsSnapshotDialogVisible] = useState(false);
  const [snapshotListData, setSnapshotListData] = useState([]);

  const snapshotInitialState = {
    action: () => {},
    apiCall: '',
    createdAt: '',
    datasetId,
    datasetSchemaId,
    description: '',
    dialogConfirmMessage: '',
    dialogConfirmQuestion: '',
    dialogMessage: '',
    isConfirmDisabled: false,
    snapShotId: ''
  };

  useEffect(() => {
    if (isSnapshotsBarVisible && !isLoadingSnapshotListData) {
      onLoadSnapshotList();
    }
  }, [isSnapshotsBarVisible]);

  const onLoadSnapshotList = async () => {
    try {
      setIsLoadingSnapshotListData(true);
      const snapshotsData = await SnapshotService.getAllDesigner(datasetId);
      setSnapshotListData(snapshotsData);
      setIsLoadingSnapshotListData(false);
    } catch (error) {
      console.error('useDatasetDesigner - onLoadSnapshotList.', error);
      notificationContext.add({ type: 'SNAPSHOT_ALL_DESIGNER_ERROR', content: { dataflowId, datasetId } });
    }
  };

  useCheckNotifications(
    ['ADD_DATASET_SCHEMA_SNAPSHOT_COMPLETED_EVENT', 'RESTORE_DATASET_SCHEMA_SNAPSHOT_COMPLETED_EVENT'],
    onLoadSnapshotList
  );

  const onCreateSnapshot = async () => {
    try {
      setIsLoadingSnapshotListData(true);
      await SnapshotService.createDesigner(datasetId, datasetSchemaId, snapshotState.description);
      snapshotDispatch({ type: 'ON_SNAPSHOT_RESET' });
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' });
      } else {
        console.error('useDatasetDesigner - onCreateSnapshot.', error);
        notificationContext.add({ type: 'CREATE_BY_ID_REPORTER_ERROR', content: { dataflowId, datasetId } });
      }
    } finally {
      setIsSnapshotDialogVisible(false);
    }
  };

  const onDeleteSnapshot = async () => {
    try {
      await SnapshotService.deleteDesigner(datasetId, snapshotState.snapShotId);
      onLoadSnapshotList();
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' });
      } else {
        console.error('useDatasetDesigner - onDeleteSnapshot.', error);
        notificationContext.add({ type: 'SNAPSHOT_DELETE_ERROR', content: { dataflowId, datasetId } });
      }
    } finally {
      setIsSnapshotDialogVisible(false);
    }
  };

  const onRestoreSnapshot = async () => {
    try {
      setIsLoadingSnapshotListData(true);
      notificationContext.add({ type: 'RESTORE_DATASET_SNAPSHOT_INIT_INFO' });
      await SnapshotService.restoreDesigner(datasetId, snapshotState.snapShotId);
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({ type: 'GENERIC_BLOCKED_ERROR' });
      } else {
        console.error('useDatasetDesigner - onRestoreSnapshot.', error);
        notificationContext.add({
          type: 'RESTORE_DATASET_SCHEMA_SNAPSHOT_FAILED_EVENT',
          content: { dataflowId, datasetId }
        });
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
    isSnapshotsBarVisible,
    setIsSnapshotsBarVisible,
    isSnapshotDialogVisible,
    setIsSnapshotDialogVisible,
    snapshotDispatch,
    snapshotListData,
    snapshotState
  };
};

export { useDatasetDesigner };
