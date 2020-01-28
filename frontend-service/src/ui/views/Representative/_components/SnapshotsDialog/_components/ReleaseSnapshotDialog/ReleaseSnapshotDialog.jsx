import React, { useContext } from 'react';

import moment from 'moment';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';

import { SnapshotService } from 'core/services/Snapshot';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const ReleaseSnapshotDialog = ({
  dataflowId,
  datasetId,
  hideReleaseDialog,
  isReleased,
  isReleasedDialogVisible,
  onLoadSnapshotList,
  setIsLoading,
  snapshotDataToRelease,
  snapshotDescription
}) => {
  const notificationContext = useContext(NotificationContext);
  const resources = useContext(ResourcesContext);

  const onBuildSnapshot = async () => {
    try {
      await SnapshotService.createByIdReporter(datasetId, snapshotDescription, isReleased);
      onLoadSnapshotList(datasetId);
    } catch (error) {
      notificationContext.add({
        type: 'CREATE_BY_ID_REPORTER_ERROR',
        content: {}
      });
    } finally {
      hideReleaseDialog();
    }
  };

  const onReleaseSnapshot = async snapshotId => {
    try {
      await SnapshotService.releaseByIdReporter(dataflowId, datasetId, snapshotId);
      onLoadSnapshotList(datasetId);
    } catch (error) {
      notificationContext.add({
        type: 'RELEASED_BY_ID_REPORTER_ERROR',
        content: {}
      });
    } finally {
      hideReleaseDialog();
    }
  };

  const releseModalFooter = (
    <div>
      <Button
        icon="cloudUpload"
        label={resources.messages['yes']}
        onClick={() =>
          (!isReleased ? onReleaseSnapshot(snapshotDataToRelease.id) : onBuildSnapshot()) && setIsLoading(true)
        }
      />
      <Button
        icon="cancel"
        className="p-button-secondary"
        label={resources.messages['no']}
        onClick={() => hideReleaseDialog()}
      />
    </div>
  );

  return (
    <Dialog
      footer={releseModalFooter}
      header={`${resources.messages['releaseSnapshotMessage']}`}
      onHide={() => hideReleaseDialog()}
      visible={isReleasedDialogVisible}>
      <ul>
        <li>
          <strong>{resources.messages['creationDate']}: </strong>
          {moment(snapshotDataToRelease.creationDate).format('YYYY-MM-DD HH:mm:ss')}
        </li>
        <li>
          <strong>{resources.messages['description']}: </strong>
          {!isReleased ? snapshotDataToRelease.description : snapshotDescription}
        </li>
      </ul>
    </Dialog>
  );
};
