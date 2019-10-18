export const snapshotReducer = (state, { type, payload }) => {
  switch (type) {
    case 'create_snapshot':
      onSetVisible(setSnapshotDialogVisible, true);
      return {
        ...state,
        snapShotId: '',
        creationDate: Date.now(),
        description: payload.description,
        dialogMessage: resources.messages.createSnapshotMessage,
        action: onCreateSnapshot
      };

    case 'delete_snapshot':
      onSetVisible(setSnapshotDialogVisible, true);
      return {
        ...state,
        snapShotId: payload.id,
        creationDate: payload.creationDate,
        description: payload.description,
        dialogMessage: resources.messages.deleteSnapshotMessage,
        action: onDeleteSnapshot
      };

    case 'release_snapshot':
      onSetVisible(setSnapshotDialogVisible, true);
      return {
        ...state,
        snapShotId: payload.id,
        creationDate: payload.creationDate,
        description: payload.description,
        dialogMessage: resources.messages.releaseSnapshotMessage,
        action: onReleaseSnapshot
      };

    case 'restore_snapshot':
      onSetVisible(setSnapshotDialogVisible, true);
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
        restored: state.snapShotId
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
