import { useContext } from 'react';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

const useSnapshotReducer = (setIsSnapshotDialogVisible, onCreateSnapshot, onDeleteSnapshot, onRestoreSnapshot) => {
  const resourcesContext = useContext(ResourcesContext);

  const snapshotReducer = (state, { type, payload }) => {
    switch (type) {
      case 'CREATE_SNAPSHOT':
        setIsSnapshotDialogVisible(true);
        return {
          ...state,
          action: onCreateSnapshot,
          creationDate: Date.now(),
          description: payload.description,
          dialogConfirmMessage: resourcesContext.messages.createSnapshotConfirmationMessage,
          dialogConfirmQuestion: resourcesContext.messages.createSnapshotConfirmationQuestion,
          dialogMessage: resourcesContext.messages.createSnapshotMessage,
          snapShotId: '',
          isConfirmDisabled: false
        };

      case 'DELETE_SNAPSHOT':
        setIsSnapshotDialogVisible(true);
        return {
          ...state,
          action: onDeleteSnapshot,
          creationDate: payload.creationDate,
          description: payload.description,
          dialogConfirmMessage: resourcesContext.messages.deleteSnapshotConfirmationMessage,
          dialogConfirmQuestion: resourcesContext.messages.deleteSnapshotConfirmationQuestion,
          dialogMessage: resourcesContext.messages.deleteSnapshotMessage,
          snapShotId: payload.id,
          isConfirmDisabled: false
        };

      case 'RESTORE_SNAPSHOT':
        setIsSnapshotDialogVisible(true);
        return {
          ...state,
          action: onRestoreSnapshot,
          creationDate: payload.creationDate,
          description: payload.description,
          dialogConfirmMessage: resourcesContext.messages.restoreSnapshotConfirmationMessage,
          dialogConfirmQuestion: resourcesContext.messages.restoreSnapshotConfirmationQuestion,
          dialogMessage: resourcesContext.messages.restoreSnapshotMessage,
          snapShotId: payload.id,
          isConfirmDisabled: false
        };

      case 'ON_SNAPSHOT_ACTION':
        return { ...state, isConfirmDisabled: true };

      case 'ON_SNAPSHOT_RESET':
        return {
          ...state,
          action: () => {},
          apiCall: '',
          createdAt: '',
          description: '',
          dialogConfirmMessage: '',
          dialogConfirmQuestion: '',
          dialogMessage: '',
          isConfirmDisabled: false,
          snapShotId: ''
        };
      default:
        return state;
    }
  };
  return { snapshotReducer };
};
export { useSnapshotReducer };
