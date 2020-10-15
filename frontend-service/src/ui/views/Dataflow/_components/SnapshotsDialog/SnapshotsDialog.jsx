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

import { useCheckNotifications } from 'ui/views/_functions/Hooks/useCheckNotifications';

export const SnapshotsDialog = ({ dataflowId, datasetId, datasetName, isSnapshotDialogVisible, manageDialogs }) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const [isActiveReleaseSnapshotConfirmDialog, setIsActiveReleaseSnapshotConfirmDialog] = useState(false);
  const [isCopyAndRelease, setIsCopyAndRelease] = useState(false);

  const [isLoading, setIsLoading] = useState(false);
  const [isReleased, setIsReleased] = useState(false);
  const [isSnapshotInputActive, setIsSnapshotInputActive] = useState(false);
  const [snapshotDataToRelease, setSnapshotDataToRelease] = useState('');
  const [snapshotIdToRelease, setSnapshotIdToRelease] = useState('');
  const [snapshotDescription, setSnapshotDescription] = useState();
  const [snapshotsListData, setSnapshotsListData] = useState([]);
  const [snapshotReleasedId, setSnapshotReleasedId] = useState('');

  useCheckNotifications(
    [
      'ADD_DATASET_SNAPSHOT_FAILED_EVENT',
      'RELEASE_DATASET_SNAPSHOT_COMPLETED_EVENT',
      'RELEASE_DATASET_SNAPSHOT_FAILED_EVENT'
    ],
    setIsLoading,
    false
  );

  const clearSnapshotToRelease = () => {
    setSnapshotDataToRelease('');
    setSnapshotIdToRelease('');
  };

  useCheckNotifications(
    [
      'RELEASE_DATASET_SNAPSHOT_COMPLETED_EVENT',
      'RELEASE_DATASET_SNAPSHOT_FAILED_EVENT',
      'DATA_COLLECTION_LOCKED_ERROR',
      'RELEASED_BY_ID_REPORTER_ERROR'
    ],
    clearSnapshotToRelease
  );

  useEffect(() => {
    onLoadSnapshotList(datasetId);
  }, []);

  useEffect(() => {
    if (isSnapshotInputActive) {
      document.getElementsByClassName('p-inputtext p-component')[0].focus();
    }
  }, [isSnapshotInputActive, isActiveReleaseSnapshotConfirmDialog]);

  const onEditorKeyChange = event => {
    if (event.key === 'Enter' && !isEmpty(snapshotDescription)) {
      event.preventDefault();
      onShowReleaseDialog({ isReleased: true });
      setIsCopyAndRelease(true);
    }
    if (event.key === 'Escape') {
      event.preventDefault();
      setIsSnapshotInputActive(false);
      setIsCopyAndRelease(false);
    }
  };

  const onHideReleaseDialog = () => {
    setIsActiveReleaseSnapshotConfirmDialog(false);
    setIsReleased(false);
    manageDialogs('isSnapshotDialogVisible', true);
    setSnapshotDescription('');
    setIsCopyAndRelease(false);
  };

  const onLoadSnapshotList = async datasetId => {
    try {
      const response = await SnapshotService.allReporter(datasetId);
      setSnapshotsListData(response);
      const snapshotReleased = response.filter(snapshot => snapshot.isReleased);
      !isEmpty(snapshotReleased) && setSnapshotReleasedId(snapshotReleased[0].id);
    } catch (error) {
      notificationContext.add({
        type: 'LOAD_SNAPSHOTS_LIST_ERROR',
        content: {}
      });
    }
  };

  useCheckNotifications(['RELEASE_DATASET_SNAPSHOT_COMPLETED_EVENT'], onLoadSnapshotList, datasetId);

  const onShowReleaseDialog = ({ isReleased }) => {
    setIsActiveReleaseSnapshotConfirmDialog(true);
    manageDialogs('isActiveReleaseSnapshotConfirmDialog', true);
    setIsReleased(isReleased);
  };

  const snapshotDialogFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon="cancel"
      label={resources.messages['close']}
      onClick={() => manageDialogs('isSnapshotDialogVisible', false)}
    />
  );

  return (
    <>
      <Dialog
        className={styles.releaseSnapshotsDialog}
        footer={snapshotDialogFooter}
        header={`${resources.messages['release']} ${datasetName}`}
        onHide={() => {
          manageDialogs('isSnapshotDialogVisible', false);
          setIsSnapshotInputActive(false);
        }}
        style={{ width: '30vw' }}
        visible={isSnapshotDialogVisible}>
        <li className={styles.createAndReleaseItem}>
          <div className={styles.itemInner}>
            <div className={styles.itemData}>
              <div className={styles.createAndReleaseText}>{resources.messages['createAndRelease']}</div>
              <div className="ui-dialog-buttonpane p-clearfix">
                {!isSnapshotInputActive ? (
                  <>
                    <Button
                      className={styles.createButton}
                      icon="plus"
                      onClick={() => {
                        setIsSnapshotInputActive(true);
                      }}
                    />
                  </>
                ) : (
                  <div className={`${styles.snapshotForm} formField ${styles.createInputAndButtonWrapper}`}>
                    <div className="p-inputgroup" style={{ width: '100%' }}>
                      <InputText
                        maxLength="255"
                        name="createSnapshotDescription"
                        onBlur={event => {
                          event.preventDefault();
                          event.isDefaultPrevented();
                          setIsSnapshotInputActive(false);
                          setIsCopyAndRelease(false);
                        }}
                        onChange={event => setSnapshotDescription(event.target.value)}
                        onKeyDown={event => onEditorKeyChange(event)}
                        placeholder={resources.messages['createSnapshotPlaceholder']}
                      />
                      <div className={styles.createButtonWrapper}>
                        <Button
                          className={styles.createSnapshotButton}
                          disabled={isEmpty(snapshotDescription)}
                          icon="cloudUpload"
                          onMouseDown={() => {
                            setIsCopyAndRelease(true);
                            onShowReleaseDialog({ isReleased: true });
                          }}
                          type="submit"
                        />
                      </div>
                    </div>
                  </div>
                )}
              </div>
            </div>
          </div>
        </li>
        <SnapshotsList
          className={styles.releaseList}
          getSnapshotData={setSnapshotDataToRelease}
          isLoading={isLoading}
          showReleaseDialog={onShowReleaseDialog}
          snapshotsListData={snapshotsListData}
          snapshotIdToRelease={snapshotIdToRelease}
          snapshotReleasedId={snapshotReleasedId}
        />
      </Dialog>
      {isActiveReleaseSnapshotConfirmDialog && (
        <ReleaseSnapshotDialog
          dataflowId={dataflowId}
          datasetId={datasetId}
          hideReleaseDialog={onHideReleaseDialog}
          isCopyAndReleaseBody={isCopyAndRelease}
          isReleased={isReleased}
          isReleasedDialogVisible={isActiveReleaseSnapshotConfirmDialog}
          onLoadSnapshotList={onLoadSnapshotList}
          setIsLoading={setIsLoading}
          setSnapshotIdToRelease={setSnapshotIdToRelease}
          snapshotDataToRelease={snapshotDataToRelease}
          snapshotDescription={snapshotDescription}
        />
      )}
    </>
  );
};
