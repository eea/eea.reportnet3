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

  const getConfirmBtnClassnames = () => {
    let classNames = '';

    if (snapshotContext.snapshotState.dialogMessage === 'Delete copy') {
      classNames = 'p-button-danger';
    }

    if (snapshotContext.snapshotState.isConfirmDisabled) {
      classNames = `${classNames} p-button-animated-blink`;
    }
    return classNames;
  };

  const onSnapshotAction = () => {
    snapshotContext.snapshotDispatch({
      type: 'ON_SNAPSHOT_ACTION'
    });
    snapshotContext.snapshotState.action();
  };

  return (
    <>
      <SnapshotSlideBar
        isLoadingSnapshotListData={isLoadingSnapshotListData}
        isSnapshotDialogVisible={isSnapshotDialogVisible}
        snapshotListData={snapshotListData}
        isReleaseVisible={isReleaseVisible}
      />

      {isSnapshotDialogVisible && (
        <ConfirmDialog
          className={styles.snapshotDialog}
          classNameConfirm={getConfirmBtnClassnames()}
          header={snapshotContext.snapshotState.dialogMessage}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          iconConfirm={snapshotContext.snapshotState.isConfirmDisabled && 'spinnerAnimate'}
          disabledConfirm={snapshotContext.snapshotState.isConfirmDisabled}
          onConfirm={onSnapshotAction}
          onHide={() => setIsSnapshotDialogVisible(false)}
          showHeader={false}
          visible={isSnapshotDialogVisible}>
          <p className={styles.dialogQuestion}>{snapshotContext.snapshotState.dialogConfirmQuestion}</p>
          <p>{snapshotContext.snapshotState.dialogConfirmMessage}</p>
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
      )}
    </>
  );
};

export { Snapshots };
