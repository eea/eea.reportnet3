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
    apiCall: '',
    createdAt: '',
    description: '',
    dialogMessage: '',
    datasetSchemaId,
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
      await SnapshotService.createByIdDesigner(datasetId, datasetSchemaId, snapshotState.description);
      onLoadSnapshotList();
    } catch (error) {
      notificationContext.add({
        type: 'CREATE_BY_ID_DESIGNER_ERROR',
        content: {
          dataflowId,
          datasetId
        }
      });
    } finally {
      setIsSnapshotDialogVisible(false);
    }
  };

  const onDeleteSnapshot = async () => {
    try {
      await SnapshotService.deleteByIdDesigner(datasetId, snapshotState.snapShotId);
      onLoadSnapshotList();
    } catch (error) {
      NotificationContext.add({
        type: 'SNAPSHOT_DELETE_ERROR',
        content: {
          dataflowId,
          datasetId
        }
      });
    } finally {
      setIsSnapshotDialogVisible(false);
    }
  };

  const onLoadSnapshotList = async () => {
    setIsLoadingSnapshotListData(true);
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
    } finally {
      setIsLoadingSnapshotListData(false);
    }
  };

  const onReleaseSnapshot = async () => {
    try {
      await SnapshotService.releaseByIdDesigner(datasetId, snapshotState.snapShotId);
      onLoadSnapshotList();
    } catch (error) {
      notificationContext.add({
        type: 'SNAPSHOT_RELEASE_ERROR',
        content: {
          dataflowId,
          datasetId
        }
      });
    } finally {
      setIsSnapshotDialogVisible(false);
    }
  };

  const onRestoreSnapshot = async () => {
    try {
      await SnapshotService.restoreByIdDesigner(datasetId, snapshotState.snapShotId);
    } catch (error) {
      notificationContext.add({
        type: 'SNAPSHOT_RESTORING_ERROR',
        content: {
          dataflowId,
          datasetId
        }
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

export { useDatasetDesigner };
