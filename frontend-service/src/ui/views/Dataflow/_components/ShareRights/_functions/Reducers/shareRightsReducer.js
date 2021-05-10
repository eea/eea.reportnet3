export const shareRightsReducer = (state, { type, payload }) => {
  switch (type) {
    case 'GET_USER_RIGHT_LIST':
      return { ...state, userRightList: payload.userRightList, clonedUserRightList: payload.clonedUserRightList };

    case 'ON_DATA_CHANGE':
      return { ...state, isDataUpdated: payload.isDataUpdated };

    case 'ON_DELETE_USER_RIGHT':
      return {
        ...state,
        isDeleteDialogVisible: payload.isDeleteDialogVisible,
        userRightToDelete: payload.userRightToDelete
      };

    case 'ON_CLOSE_MANAGEMENT_DIALOG':
      return {
        ...state,
        userRightToEdit: null,
        userRightToAdd: null,
        isEditing: false
      };

    case 'ON_SET_ACCOUNT':
      return {
        ...state,
        userRightList: payload.userRightList,
        accountHasError: payload.accountHasError,
        accountNotFound: payload.accountNotFound
      };

    case 'ON_ROLE_CHANGE':
      return { ...state, userRightList: payload.userRightList };

    case 'SET_ACCOUNT_HAS_ERROR':
      return { ...state, accountHasError: payload.accountHasError };

    case 'ON_EDIT_USER_RIGHT':
      return { ...state, isEditing: payload.isEditing, userRight: payload.userRight };

    case 'SET_ACCOUNT_NOT_FOUND':
      return { ...state, accountNotFound: payload.accountNotFound, accountHasError: payload.accountHasError };

    case 'SET_IS_VISIBLE_DELETE_CONFIRM_DIALOG':
      return { ...state, isDeleteDialogVisible: payload.isDeleteDialogVisible };

    case 'TOGGLE_DELETING_USER_RIGHT':
      return { ...state, isDeletingUserRight: payload.isDeleting };

    default:
      return state;
  }
};
