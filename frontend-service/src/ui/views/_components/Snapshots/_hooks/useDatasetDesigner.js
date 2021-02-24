import { useState, useContext, useEffect, useReducer } from 'react';

import { useSnapshotReducer } from './useSnapshotReducer';
import { SnapshotService } from 'core/services/Snapshot';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

const useDatasetDesigner = (dataflowId, datasetId, datasetSchemaId) => {
  const notificationContext = useContext(NotificationContext);
  const [isLoadingSnapshotListData, setIsLoadingSnapshotListData] = useState(true);
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
    if (isSnapshotsBarVisible) {
      onLoadSnapshotList();
    }
  }, [isSnapshotsBarVisible]);

  const onCreateSnapshot = async () => {
    try {
      await SnapshotService.createByIdDesigner(datasetId, datasetSchemaId, snapshotState.description);
      onLoadSnapshotList();
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({
          type: 'SNAPSHOT_ACTION_BLOCKED_ERROR'
        });
      } else {
        notificationContext.add({
          type: 'CREATE_BY_ID_REPORTER_ERROR',
          content: {
            dataflowId,
            datasetId
          }
        });
      }
    } finally {
      setIsSnapshotDialogVisible(false);
    }
  };

  const onDeleteSnapshot = async () => {
    try {
      await SnapshotService.deleteByIdDesigner(datasetId, snapshotState.snapShotId);
      onLoadSnapshotList();
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({
          type: 'SNAPSHOT_ACTION_BLOCKED_ERROR'
        });
      } else {
        notificationContext.add({
          type: 'SNAPSHOT_DELETE_ERROR',
          content: {
            dataflowId,
            datasetId
          }
        });
      }
    } finally {
      setIsSnapshotDialogVisible(false);
    }
  };

  const onLoadSnapshotList = async () => {
    try {
      //Settimeout for avoiding the overlaping between the slidebar transition and the api call
      setTimeout(async () => {
        const snapshotsData = await SnapshotService.allDesigner(datasetId);

        setSnapshotListData(snapshotsData);
        setIsLoadingSnapshotListData(false);
      }, 500);
    } catch (error) {
      notificationContext.add({
        type: 'SNAPSHOT_ALL_DESIGNER_ERROR',
        content: {
          dataflowId,
          datasetId
        }
      });
    }
  };

  const onRestoreSnapshot = async () => {
    try {
      notificationContext.add({
        type: 'RESTORE_DATASET_SNAPSHOT_INIT_INFO'
      });
      await SnapshotService.restoreByIdDesigner(datasetId, snapshotState.snapShotId);
    } catch (error) {
      if (error.response.status === 423) {
        notificationContext.add({
          type: 'SNAPSHOT_ACTION_BLOCKED_ERROR'
        });
      } else {
        notificationContext.add({
          type: 'RESTORE_DATASET_SCHEMA_SNAPSHOT_FAILED_EVENT',
          content: {
            dataflowId,
            datasetId
          }
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
