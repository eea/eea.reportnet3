import React, { useContext } from 'react';

import moment from 'moment';

import styles from './Snapshots.module.scss';

import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { SnapshotContext } from 'ui/views/_functions/Contexts/SnapshotContext';
import { SnapshotSlideBar } from './_components/SnapshotSlideBar';

const Snapshots = ({
  isReleaseVisible = false,
  isLoadingSnapshotListData,
  isSnapshotDialogVisible,
  setIsSnapshotDialogVisible,
  snapshotListData
}) => {
  const resources = useContext(ResourcesContext);
  const snapshotContext = useContext(SnapshotContext);

  return (
    <>
      <SnapshotSlideBar
        isLoadingSnapshotListData={isLoadingSnapshotListData}
        setIsSnapshotDialogVisible={setIsSnapshotDialogVisible}
        snapshotListData={snapshotListData}
        isReleaseVisible={isReleaseVisible}
      />

      <ConfirmDialog
        className={styles.snapshotDialog}
        header={snapshotContext.snapshotState.dialogMessage}
        labelCancel={resources.messages['no']}
        labelConfirm={resources.messages['yes']}
        maximizable={false}
        onConfirm={snapshotContext.snapshotState.action}
        onHide={() => setIsSnapshotDialogVisible(false)}
        showHeader={false}
        visible={isSnapshotDialogVisible}>
        <ul>
          <li>
            <strong>{resources.messages.creationDate}: </strong>
            {moment(snapshotContext.snapshotState.creationDate).format('YYYY-MM-DD HH:mm:ss')}
          </li>
          <li>
            <strong>{resources.messages.description}: </strong>
            {snapshotContext.snapshotState.description}
          </li>
        </ul>
      </ConfirmDialog>
    </>
  );
};

export { Snapshots };
