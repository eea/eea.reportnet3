const SnapshotFunctions = () => {
  onLoadSnapshotList = async () => {
    try {
      setIsLoadingSnapshotListData(true);
      //Settimeout for avoiding the overlaping between the slidebar transition and the api call
      setTimeout(async () => {
        const snapshotsData = await SnapshotService.all(datasetId);
        setSnapshotListData(snapshotsData);
        setIsLoadingSnapshotListData(false);
      }, 500);
    } catch (error) {
      setIsLoadingSnapshotListData(false);
    }
  };

  const onCreateSnapshot = async () => {
    const snapshotCreated = await SnapshotService.createById(datasetId, snapshotState.description);
    if (snapshotCreated) {
      onLoadSnapshotList();
    }
    onSetVisible(setSnapshotDialogVisible, false);
  };

  const onDeleteSnapshot = async () => {
    const snapshotDeleted = await SnapshotService.deleteById(datasetId, snapshotState.snapShotId);
    if (snapshotDeleted) {
      onLoadSnapshotList();
    }
    onSetVisible(setSnapshotDialogVisible, false);
  };

  const onReleaseSnapshot = async () => {
    const snapshotReleased = await SnapshotService.releaseById(dataflowId, datasetId, snapshotState.snapShotId);
    if (snapshotReleased) {
      onLoadSnapshotList();
    }
    onSetVisible(setSnapshotDialogVisible, false);
  };

  const onRestoreSnapshot = async () => {
    const response = await SnapshotService.restoreById(dataflowId, datasetId, snapshotState.snapShotId);

    if (response) {
      snapshotDispatch({ type: 'mark_as_restored', payload: {} });
      onGrowlAlert({
        severity: 'info',
        summary: resources.messages.snapshotItemRestoreProcessSummary,
        detail: resources.messages.snapshotItemRestoreProcessDetail,
        life: '5000'
      });
    }

    onSetVisible(setSnapshotDialogVisible, false);
  };
};

export { SnapshotFunctions };
