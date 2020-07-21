import { useContext } from 'react';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const useSnapshotReducer = (
  setIsSnapshotDialogVisible,
  onCreateSnapshot,
  onDeleteSnapshot,
  onRestoreSnapshot,
  onReleaseSnapshot
) => {
  const resources = useContext(ResourcesContext);

  const snapshotReducer = (state, { type, payload }) => {
    switch (type) {
      case 'CREATE_SNAPSHOT':
        setIsSnapshotDialogVisible(true);

        return {
          ...state,
          snapShotId: '',
          creationDate: Date.now(),
          description: payload.description,
          dialogMessage: resources.messages.createSnapshotMessage,
          dialogConfirmMessage: resources.messages.createSnapshotConfirmationMessage,
          dialogConfirmQuestion: resources.messages.createSnapshotConfirmationQuestion,
          action: onCreateSnapshot
        };

      case 'DELETE_SNAPSHOT':
        setIsSnapshotDialogVisible(true);

        return {
          ...state,
          snapShotId: payload.id,
          creationDate: payload.creationDate,
          description: payload.description,
          dialogMessage: resources.messages.deleteSnapshotMessage,
          dialogConfirmMessage: resources.messages.deleteSnapshotConfirmationMessage,
          dialogConfirmQuestion: resources.messages.deleteSnapshotConfirmationQuestion,
          action: onDeleteSnapshot
        };

      case 'RELEASE_SNAPSHOT':
        setIsSnapshotDialogVisible(true);

        return {
          ...state,
          snapShotId: payload.id,
          creationDate: payload.creationDate,
          description: payload.description,
          dialogMessage: resources.messages.releaseSnapshotMessage,
          dialogConfirmMessage: resources.messages.releaseSnapshotConfirmationMessage,
          dialogConfirmQuestion: resources.messages.releaseSnapshotConfirmationQuestion,
          action: onReleaseSnapshot
        };

      case 'RESTORE_SNAPSHOT':
        setIsSnapshotDialogVisible(true);

        return {
          ...state,
          snapShotId: payload.id,
          creationDate: payload.creationDate,
          description: payload.description,
          dialogMessage: resources.messages.restoreSnapshotMessage,
          dialogConfirmMessage: resources.messages.restoreSnapshotConfirmationMessage,
          dialogConfirmQuestion: resources.messages.restoreSnapshotConfirmationQuestion,
          action: onRestoreSnapshot
        };

      default:
        return state;
    }
  };
  return { snapshotReducer };
};
export { useSnapshotReducer };
