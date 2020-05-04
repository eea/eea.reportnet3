import React, { useContext } from 'react';

import moment from 'moment';

import styles from './Snapshots.module.scss';

import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { SnapshotSlideBar } from './_components/SnapshotSlideBar';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { SnapshotContext } from 'ui/views/_functions/Contexts/SnapshotContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

const Snapshots = ({
  isReleaseVisible = false,
  isLoadingSnapshotListData,
  isSnapshotDialogVisible,
  setIsSnapshotDialogVisible,
  snapshotListData
}) => {
  const resources = useContext(ResourcesContext);
  const snapshotContext = useContext(SnapshotContext);
  const userContext = useContext(UserContext);

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
        classNameConfirm={snapshotContext.snapshotState.dialogMessage === 'Delete copy' ? 'p-button-danger' : undefined}
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
            {moment(snapshotContext.snapshotState.creationDate).format(
              `${userContext.userProps.dateFormat} ${userContext.userProps.amPm24h ? 'HH' : 'hh'}:mm:ss${
                userContext.userProps.amPm24h ? '' : ' A'
              }`
            )}
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
