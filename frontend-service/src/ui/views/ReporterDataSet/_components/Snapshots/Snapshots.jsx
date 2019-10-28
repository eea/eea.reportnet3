import React, { useState, useContext, useReducer, useEffect } from 'react';

import moment from 'moment';

import styles from './Snapshots.module.scss';

import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { SnapshotContext } from 'ui/views/_components/_context/SnapshotContext';
import { SnapshotService } from 'core/services/Snapshot';
import { SnapshotSlideBar } from './SnapshotSlideBar';

const Snapshots = ({ datasetId, dataflowId, growlRef, isSnapshotsBarVisible, setIsSnapshotsBarVisible }) => {
  const resources = useContext(ResourcesContext);

  const [isLoadingSnapshotListData, setIsLoadingSnapshotListData] = useState(true);
  const [snapshotDialogVisible, setSnapshotDialogVisible] = useState(false);
  const [snapshotListData, setSnapshotListData] = useState([]);

  useEffect(() => {
    onLoadSnapshotList();
  }, [isSnapshotsBarVisible]);

  const onGrowlAlert = message => {
    growlRef.current.show(message);
  };

  const onCreateSnapshot = async () => {
    const snapshotToCreate = await SnapshotService.createById(datasetId, snapshotState.description);

    if (snapshotToCreate.isCreated) {
      onLoadSnapshotList();
    }

    setSnapshotDialogVisible(false);
  };

  const onDeleteSnapshot = async () => {
    const snapshotToDelete = await SnapshotService.deleteById(datasetId, snapshotState.snapShotId);

    if (snapshotToDelete.isDeleted) {
      onLoadSnapshotList();
    }

    setSnapshotDialogVisible(false);
  };

  const onLoadSnapshotList = async () => {
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

  const onReleaseSnapshot = async () => {
    const snapshotToRelease = await SnapshotService.releaseById(dataflowId, datasetId, snapshotState.snapShotId);

    if (snapshotToRelease.isReleased) {
      onLoadSnapshotList();
    }

    setSnapshotDialogVisible(false);
  };

  const onRestoreSnapshot = async () => {
    const snapshotToRestore = await SnapshotService.restoreById(dataflowId, datasetId, snapshotState.snapShotId);

    if (snapshotToRestore.isRestored) {
      snapshotDispatch({ type: 'mark_as_restored', payload: {} });

      onGrowlAlert({
        severity: 'info',
        summary: resources.messages.snapshotItemRestoreProcessSummary,
        detail: resources.messages.snapshotItemRestoreProcessDetail,
        life: '5000'
      });
    }

    setSnapshotDialogVisible(false);
  };

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

  const snapshotReducer = (state, { type, payload }) => {
    switch (type) {
      case 'create_snapshot':
        setSnapshotDialogVisible(true);

        return {
          ...state,
          snapShotId: '',
          creationDate: Date.now(),
          description: payload.description,
          dialogMessage: resources.messages.createSnapshotMessage,
          action: onCreateSnapshot
        };

      case 'delete_snapshot':
        setSnapshotDialogVisible(true);

        return {
          ...state,
          snapShotId: payload.id,
          creationDate: payload.creationDate,
          description: payload.description,
          dialogMessage: resources.messages.deleteSnapshotMessage,
          action: onDeleteSnapshot
        };

      case 'release_snapshot':
        setSnapshotDialogVisible(true);

        return {
          ...state,
          snapShotId: payload.id,
          creationDate: payload.creationDate,
          description: payload.description,
          dialogMessage: resources.messages.releaseSnapshotMessage,
          action: onReleaseSnapshot
        };

      case 'restore_snapshot':
        setSnapshotDialogVisible(true);

        return {
          ...state,
          snapShotId: payload.id,
          creationDate: payload.creationDate,
          description: payload.description,
          dialogMessage: resources.messages.restoreSnapshotMessage,
          action: onRestoreSnapshot
        };

      case 'mark_as_restored':
        return {
          ...state,
          restored: state.snapShotId
        };

      case 'clear_restored':
        return {
          ...state,
          restored: undefined
        };

      default:
        return state;
    }
  };

  const [snapshotState, snapshotDispatch] = useReducer(snapshotReducer, snapshotInitialState);

  return (
    <>
      <SnapshotContext.Provider
        value={{
          snapshotState: snapshotState,
          snapshotDispatch: snapshotDispatch
        }}>
        <SnapshotSlideBar
          isVisible={isSnapshotsBarVisible}
          isLoadingSnapshotListData={isLoadingSnapshotListData}
          setIsVisible={setIsSnapshotsBarVisible}
          setSnapshotDialogVisible={setSnapshotDialogVisible}
          snapshotListData={snapshotListData}
        />

        <ConfirmDialog
          className={styles.snapshotDialog}
          header={snapshotState.dialogMessage}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          maximizable={false}
          onConfirm={snapshotState.action}
          onHide={() => setSnapshotDialogVisible(false)}
          showHeader={false}
          visible={snapshotDialogVisible}>
          <ul>
            <li>
              <strong>{resources.messages.creationDate}: </strong>
              {moment(snapshotState.creationDate).format('DD/MM/YYYY HH:mm:ss')}
            </li>
            <li>
              <strong>{resources.messages.description}: </strong>
              {snapshotState.description}
            </li>
          </ul>
        </ConfirmDialog>
      </SnapshotContext.Provider>
    </>
  );
};

export { Snapshots };
