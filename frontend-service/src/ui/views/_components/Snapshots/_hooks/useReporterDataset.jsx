import { useState, useContext, useEffect, useReducer } from 'react';

import { useSnapshotReducer } from './useSnapshotReducer';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { SnapshotService } from 'core/services/Snapshot';

const useReporterDataset = (datasetId, dataflowId, growlRef) => {
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

  const onGrowlAlert = message => {
    growlRef.current.show(message);
  };

  const onCreateSnapshot = async () => {
    const snapshotToCreate = await SnapshotService.createByIdReporter(datasetId, snapshotState.description);

    if (snapshotToCreate.isCreated) {
      onLoadSnapshotList();
    }

    setIsSnapshotDialogVisible(false);
  };

  const onDeleteSnapshot = async () => {
    const snapshotToDelete = await SnapshotService.deleteByIdReporter(datasetId, snapshotState.snapShotId);

    if (snapshotToDelete.isDeleted) {
      onLoadSnapshotList();
    }

    setIsSnapshotDialogVisible(false);
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
      setIsLoadingSnapshotListData(false);
    }
  };

  const onReleaseSnapshot = async () => {
    const snapshotToRelease = await SnapshotService.releaseByIdReporter(
      dataflowId,
      datasetId,
      snapshotState.snapShotId
    );

    if (snapshotToRelease.isReleased) {
      onLoadSnapshotList();
    }

    setIsSnapshotDialogVisible(false);
  };

  const onRestoreSnapshot = async () => {
    const snapshotToRestore = await SnapshotService.restoreByIdReporter(
      dataflowId,
      datasetId,
      snapshotState.snapShotId
    );

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

export { useReporterDataset };
