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
          action: onCreateSnapshot,
          creationDate: Date.now(),
          description: payload.description,
          dialogConfirmMessage: resources.messages.createSnapshotConfirmationMessage,
          dialogConfirmQuestion: resources.messages.createSnapshotConfirmationQuestion,
          dialogMessage: resources.messages.createSnapshotMessage,
          snapShotId: ''
        };

      case 'DELETE_SNAPSHOT':
        setIsSnapshotDialogVisible(true);

        return {
          ...state,
          action: onDeleteSnapshot,
          creationDate: payload.creationDate,
          description: payload.description,
          dialogConfirmMessage: resources.messages.deleteSnapshotConfirmationMessage,
          dialogConfirmQuestion: resources.messages.deleteSnapshotConfirmationQuestion,
          dialogMessage: resources.messages.deleteSnapshotMessage,
          snapShotId: payload.id
        };

      case 'RELEASE_SNAPSHOT':
        setIsSnapshotDialogVisible(true);

        return {
          ...state,
          action: onReleaseSnapshot,
          creationDate: payload.creationDate,
          description: payload.description,
          dialogConfirmMessage: resources.messages.releaseSnapshotConfirmationMessage,
          dialogConfirmQuestion: resources.messages.releaseSnapshotConfirmationQuestion,
          dialogMessage: resources.messages.releaseSnapshotMessage,
          snapShotId: payload.id
        };

      case 'RESTORE_SNAPSHOT':
        setIsSnapshotDialogVisible(true);

        return {
          ...state,
          action: onRestoreSnapshot,
          creationDate: payload.creationDate,
          description: payload.description,
          dialogConfirmMessage: resources.messages.restoreSnapshotConfirmationMessage,
          dialogConfirmQuestion: resources.messages.restoreSnapshotConfirmationQuestion,
          dialogMessage: resources.messages.restoreSnapshotMessage,
          snapShotId: payload.id
        };

      default:
        return state;
    }
  };
  return { snapshotReducer };
};
export { useSnapshotReducer };
