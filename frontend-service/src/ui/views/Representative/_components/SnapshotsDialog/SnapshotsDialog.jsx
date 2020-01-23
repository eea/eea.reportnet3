import React, { useContext, useEffect, useState } from 'react';

import { isEmpty } from 'lodash';

import styles from './SnapshotsDialog.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { InputText } from 'ui/views/_components/InputText';
import { ReleaseSnapshotDialog } from './_components/ReleaseSnapshotDialog';
import { SnapshotsList } from './_components/SnapshotsList';

import { SnapshotService } from 'core/services/Snapshot';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const SnapshotsDialog = ({
  dataflowData,
  dataflowId,
  datasetId,
  hideSnapshotDialog,
  isSnapshotDialogVisible,
  setSnapshotDialog
}) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [isActiveReleaseSnapshotConfirmDialog, setIsActiveReleaseSnapshotConfirmDialog] = useState(false);
  const [isReleased, setIsReleased] = useState(false);
  const [isSnapshotInputActive, setIsSnapshotInputActive] = useState(false);
  const [snapshotDataToRelease, setSnapshotDataToRelease] = useState('');
  const [snapshotsListData, setSnapshotsListData] = useState([]);
  const [snapshotDescription, setSnapshotDescription] = useState();

  useEffect(() => {
    if (isSnapshotDialogVisible) {
      onLoadSnapshotList(datasetId);
    }
    const refresh = notificationContext.toShow.find(
      notification => notification.key === 'RELEASE_DATASET_SNAPSHOT_COMPLETED_EVENT'
    );
    if (refresh) {
      onLoadSnapshotList(datasetId);
    }
  }, [isSnapshotDialogVisible, notificationContext]);

  useEffect(() => {
    document.getElementsByClassName('p-inputtext p-component')[0].focus();
  }, [isSnapshotInputActive, isActiveReleaseSnapshotConfirmDialog]);

  const onHideReleaseDialog = () => {
    setIsActiveReleaseSnapshotConfirmDialog(false);
    setIsReleased(false);
    setSnapshotDialog(true);
  };

  const onLoadSnapshotList = async datasetId => {
    setSnapshotsListData(await SnapshotService.allReporter(datasetId));
  };

  const onShowReleaseDialog = ({ isRelease }) => {
    setIsActiveReleaseSnapshotConfirmDialog(true);
    setSnapshotDialog(false);
    setIsReleased(isRelease);
  };

  const snapshotDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      {!isSnapshotInputActive ? (
        <>
          <Button
            className={styles.createButton}
            label={resources.messages['createAndRelease']}
            icon="check"
            onClick={() => setIsSnapshotInputActive(true)}
          />
          <Button
            className="p-button-secondary"
            label={resources.messages['close']}
            icon="cancel"
            onClick={() => hideSnapshotDialog()}
          />
        </>
      ) : (
        <div className={`${styles.snapshotForm} formField ${styles.createInputAndButtonWrapper}`}>
          <div className="p-inputgroup" style={{ width: '100%' }}>
            <InputText
              name="createSnapshotDescription"
              onChange={event => setSnapshotDescription(event.target.value)}
              placeholder={resources.messages.createSnapshotPlaceholder}
            />
            <div className={styles.createButtonWrapper}>
              <Button
                className={styles.createSnapshotButton}
                disabled={isEmpty(snapshotDescription)}
                icon="cloudUpload"
                onClick={() => onShowReleaseDialog({ isRelease: true })}
                tooltip={resources.messages['createAndRelease']}
                type="submit"
              />
            </div>
          </div>
          <Button
            className="p-button-secondary"
            icon="cancel"
            label={resources.messages['close']}
            onClick={() => hideSnapshotDialog()}
          />
        </div>
      )}
    </div>
  );

  return (
    <>
      <Dialog
        className={styles.releaseSnapshotsDialog}
        footer={snapshotDialogFooter}
        header={`${resources.messages['snapshots'].toUpperCase()} ${dataflowData.name.toUpperCase()}`}
        onHide={() => {
          hideSnapshotDialog();
          setIsSnapshotInputActive(false);
        }}
        style={{ width: '30vw' }}
        visible={isSnapshotDialogVisible}>
        <SnapshotsList
          className={styles.releaseList}
          getSnapshotData={setSnapshotDataToRelease}
          showReleaseDialog={onShowReleaseDialog}
          snapshotsListData={snapshotsListData}
        />
      </Dialog>
      <ReleaseSnapshotDialog
        dataflowId={dataflowId}
        datasetId={datasetId}
        isReleasedDialogVisible={isActiveReleaseSnapshotConfirmDialog}
        isReleased={isReleased}
        hideReleaseDialog={onHideReleaseDialog}
        onLoadSnapshotList={onLoadSnapshotList}
        snapshotDataToRelease={snapshotDataToRelease}
        snapshotDescription={snapshotDescription}
      />
    </>
  );
};
