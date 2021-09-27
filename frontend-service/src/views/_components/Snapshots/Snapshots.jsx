import { Fragment, useContext } from 'react';

import dayjs from 'dayjs';

import styles from './Snapshots.module.scss';

import { ConfirmDialog } from 'views/_components/ConfirmDialog';
import { SnapshotSlideBar } from './_components/SnapshotSlideBar';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { SnapshotContext } from 'views/_functions/Contexts/SnapshotContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

const Snapshots = ({
  isLoadingSnapshotListData,
  isSnapshotDialogVisible,
  setIsSnapshotDialogVisible,
  snapshotListData
}) => {
  const resourcesContext = useContext(ResourcesContext);
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
    <Fragment>
      <SnapshotSlideBar
        isLoadingSnapshotListData={isLoadingSnapshotListData}
        isSnapshotDialogVisible={isSnapshotDialogVisible}
        snapshotListData={snapshotListData}
      />

      {isSnapshotDialogVisible && (
        <ConfirmDialog
          className={styles.snapshotDialog}
          classNameConfirm={getConfirmBtnClassnames()}
          disabledConfirm={snapshotContext.snapshotState.isConfirmDisabled}
          header={snapshotContext.snapshotState.dialogMessage}
          iconConfirm={snapshotContext.snapshotState.isConfirmDisabled && 'spinnerAnimate'}
          labelCancel={resourcesContext.messages['no']}
          labelConfirm={resourcesContext.messages['yes']}
          onConfirm={onSnapshotAction}
          onHide={() => setIsSnapshotDialogVisible(false)}
          showHeader={false}
          visible={isSnapshotDialogVisible}>
          <p className={styles.dialogQuestion}>{snapshotContext.snapshotState.dialogConfirmQuestion}</p>
          <p>{snapshotContext.snapshotState.dialogConfirmMessage}</p>
          <ul>
            <li>
              <strong>{resourcesContext.messages.creationDate}: </strong>
              {dayjs(snapshotContext.snapshotState.creationDate).format(
                `${userContext.userProps.dateFormat} ${userContext.userProps.amPm24h ? 'HH' : 'hh'}:mm:ss${
                  userContext.userProps.amPm24h ? '' : ' A'
                }`
              )}
            </li>
            <li>
              <strong>{resourcesContext.messages.description}: </strong>
              <span className={styles.description}>{snapshotContext.snapshotState.description}</span>
            </li>
          </ul>
        </ConfirmDialog>
      )}
    </Fragment>
  );
};

export { Snapshots };
