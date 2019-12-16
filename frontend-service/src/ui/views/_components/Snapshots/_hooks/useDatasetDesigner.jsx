import { useState, useContext, useEffect, useReducer } from 'react';

import { useSnapshotReducer } from './useSnapshotReducer';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { SnapshotService } from 'core/services/Snapshot';
import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

const useDatasetDesigner = (dataflowId, datasetId, datasetSchemaId, growlRef) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
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

  const onGrowlAlert = message => {
    growlRef.current.show(message);
  };

  const onCreateSnapshot = async () => {
    try {
      const snapshotToCreate = await SnapshotService.createByIdDesigner(
        datasetId,
        datasetSchemaId,
        snapshotState.description
      );
      if (snapshotToCreate.status >= 200 && snapshotToCreate.status <= 299) {
        onLoadSnapshotList();
      } else {
        notificationContext.add({
          type: 'SNAPSHOT_CREATION_ERROR',
          content: {
            dataflowId,
            datasetId
          }
        });
      }
    } catch (error) {
      notificationContext.add({
        type: 'SNAPSHOT_CREATION_ERROR',
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
      const snapshotToDelete = await SnapshotService.deleteByIdDesigner(datasetId, snapshotState.snapShotId);
      if (snapshotToDelete.status >= 200 && snapshotToDelete.status <= 299) {
        onLoadSnapshotList();
      } else {
        NotificationContext.add({
          type: 'SNAPSHOT_DELETE_ERROR',
          content: {
            dataflowId,
            datasetId
          }
        });
      }
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
    try {
      setIsLoadingSnapshotListData(true);

      //Settimeout for avoiding the overlaping between the slidebar transition and the api call
      setTimeout(async () => {
        const snapshotsData = await SnapshotService.allDesigner(datasetId);

        setSnapshotListData(snapshotsData);

        setIsLoadingSnapshotListData(false);
      }, 500);
    } catch (error) {
      setIsLoadingSnapshotListData(false);
    }
  };

  const onReleaseSnapshot = async () => {
    const snapshotToRelease = await SnapshotService.releaseByIdDesigner(datasetId, snapshotState.snapShotId);

    if (snapshotToRelease.isReleased) {
      onLoadSnapshotList();
    }

    setIsSnapshotDialogVisible(false);
  };

  const onRestoreSnapshot = async () => {
    const snapshotToRestore = await SnapshotService.restoreByIdDesigner(datasetId, snapshotState.snapShotId);

    if (snapshotToRestore.isRestored) {
      snapshotDispatch({ type: 'mark_as_restored', payload: {} });

      onGrowlAlert({
        severity: 'info',
        summary: resources.messages.snapshotItemRestoreProcessSummary,
        detail: resources.messages.snapshotItemRestoreProcessDetail,
        life: '5000'
      });
    }

    setIsSnapshotDialogVisible(false);
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
