import React, { useContext } from 'react';

import moment from 'moment';

import styles from './ReleaseSnapshotDialog.module.scss';

import DataflowConf from 'conf/dataflow.config.json';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';

import { SnapshotService } from 'core/services/Snapshot';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

export const ReleaseSnapshotDialog = ({
  dataflowId,
  datasetId,
  hideReleaseDialog,
  isCopyAndReleaseBody,
  isReleased,
  isReleasedDialogVisible,
  onLoadSnapshotList,
  setIsLoading,
  snapshotDataToRelease,
  snapshotDescription
}) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const onBuildSnapshot = async () => {
    setIsLoading(true);
    try {
      await SnapshotService.createByIdReporter(datasetId, snapshotDescription, isReleased);
      onLoadSnapshotList(datasetId);
    } catch (error) {
      setIsLoading(false);
      if (error.response.data == DataflowConf.errorTypes['copyWithErrors']) {
        notificationContext.add({ type: 'RELEASE_BLOCKED_EVENT' });
      } else if (error.response.status === 423) {
        notificationContext.add({ type: 'DATA_COLLECTION_LOCKED_ERROR' });
      } else {
        notificationContext.add({ type: 'CREATE_BY_ID_REPORTER_ERROR', content: {} });
      }
    } finally {
      hideReleaseDialog();
    }
  };

  const onKeyPress = event => {
    if (event.key === 'Enter') {
      !isReleased ? onReleaseSnapshot(snapshotDataToRelease.id) : onBuildSnapshot();
    }
  };

  const onReleaseSnapshot = async snapshotId => {
    setIsLoading(true);
    try {
      await SnapshotService.releaseByIdReporter(dataflowId, datasetId, snapshotId);
      onLoadSnapshotList(datasetId);
    } catch (error) {
      setIsLoading(false);

      if (error.response.status === 423) {
        notificationContext.add({ type: 'DATA_COLLECTION_LOCKED_ERROR' });
      } else {
        notificationContext.add({ type: 'RELEASED_BY_ID_REPORTER_ERROR', content: {} });
      }
    } finally {
      hideReleaseDialog();
    }
  };

  const releaseModalFooter = (
    <div>
      <Button
        icon="cloudUpload"
        label={resources.messages['yes']}
        onClick={() => (!isReleased ? onReleaseSnapshot(snapshotDataToRelease.id) : onBuildSnapshot())}
      />
      <Button
        icon="cancel"
        className="p-button-secondary"
        label={resources.messages['no']}
        onClick={() => hideReleaseDialog()}
      />
    </div>
  );

  const releaseBody = (
    <div>
      <p>
        <span className={styles.confirmReleaseSpan}>{resources.messages['confirmReleaseCopy']}</span>
      </p>
      <p>
        <span>{resources.messages['confirmReleaseCopyIntroduction']}</span>
      </p>
      <ul>
        <li>
          <strong>{resources.messages['description']}: </strong>
          {!isReleased ? snapshotDataToRelease.description : snapshotDescription}
        </li>
        <li>
          <strong>{resources.messages['creationDate']}: </strong>
          {moment(snapshotDataToRelease.creationDate).format(
            `${userContext.userProps.dateFormat} ${userContext.userProps.amPm24h ? 'HH' : 'hh'}:mm:ss${
              userContext.userProps.amPm24h ? '' : ' A'
            }`
          )}
        </li>
      </ul>
    </div>
  );

  const copyAndReleaseBody = (
    <div>
      <p>
        <span className={styles.confirmReleaseSpan}>{resources.messages['confirmReleaseCurrentData']}</span>
        <span> {resources.messages['confirmReleaseAndCopyExtraMessage']}</span>
      </p>
      <p>
        <span>{resources.messages['confirmReleaseCurrentDataIntroduction']}</span>
      </p>
      <ul>
        <li>
          <strong>{resources.messages['description']}: </strong>
          {!isReleased ? snapshotDataToRelease.description : snapshotDescription}
        </li>
        <li>
          <strong>{resources.messages['creationDate']}: </strong>
          {moment(snapshotDataToRelease.creationDate).format(
            `${userContext.userProps.dateFormat} ${userContext.userProps.amPm24h ? 'HH' : 'hh'}:mm:ss${
              userContext.userProps.amPm24h ? '' : ' A'
            }`
          )}
        </li>
      </ul>
    </div>
  );

  return (
    <div onKeyPress={event => onKeyPress(event)}>
      {isReleasedDialogVisible && (
        <Dialog
          footer={releaseModalFooter}
          header={`${resources.messages['releaseSnapshotMessage']}`}
          onHide={() => hideReleaseDialog()}
          visible={isReleasedDialogVisible}>
          <div>{isCopyAndReleaseBody ? copyAndReleaseBody : releaseBody}</div>
        </Dialog>
      )}
    </div>
  );
};
