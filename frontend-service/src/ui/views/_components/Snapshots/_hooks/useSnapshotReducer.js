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
      case 'create_snapshot':
        setIsSnapshotDialogVisible(true);

        return {
          ...state,
          snapShotId: '',
          creationDate: Date.now(),
          description: payload.description,
          dialogMessage: resources.messages.createSnapshotMessage,
          action: onCreateSnapshot
        };

      case 'delete_snapshot':
        setIsSnapshotDialogVisible(true);

        return {
          ...state,
          snapShotId: payload.id,
          creationDate: payload.creationDate,
          description: payload.description,
          dialogMessage: resources.messages.deleteSnapshotMessage,
          action: onDeleteSnapshot
        };

      case 'release_snapshot':
        setIsSnapshotDialogVisible(true);

        return {
          ...state,
          snapShotId: payload.id,
          creationDate: payload.creationDate,
          description: payload.description,
          dialogMessage: resources.messages.releaseSnapshotMessage,
          action: onReleaseSnapshot
        };

      case 'restore_snapshot':
        setIsSnapshotDialogVisible(true);

        return {
          ...state,
          snapShotId: payload.id,
          creationDate: payload.creationDate,
          description: payload.description,
          dialogMessage: resources.messages.restoreSnapshotMessage,
          action: onRestoreSnapshot
        };

      case 'mark_as_restored':
        return {
          ...state,
          restored: state.snapshotId
        };

      case 'clear_restored':
        return {
          ...state,
          restored: undefined
        };

      default:
        return state;
    }
  };
  return { snapshotReducer };
};
export { useSnapshotReducer };
