export const shareRightsReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ADD_USER':
      return { ...state, account: payload.email };

    case 'GET_ALL_USERS':
      return { ...state, users: payload.users, clonedUsers: payload.clonedUsers };

    case 'ON_DATA_CHANGE':
      return { ...state, isDataUpdated: payload.isDataUpdated };

    case 'ON_DELETE_USER':
      return {
        ...state,
        isDeleteDialogVisible: payload.isDeleteDialogVisible,
        userAccountToDelete: payload.userAccountToDelete
      };

    case 'ON_SET_ACCOUNT':
      return {
        ...state,
        users: payload.users,
        accountHasError: payload.accountHasError,
        accountNotFound: payload.accountNotFound
      };

    case 'ON_WRITE_PERMISSION_CHANGE':
      return { ...state, users: payload.users };

    case 'SET_ACCOUNT_HAS_ERROR':
      return { ...state, accountHasError: payload.accountHasError };

    case 'SET_USER_TO_UPDATE':
      return { ...state, user: payload.user };

    case 'SET_ACCOUNT_NOT_FOUND':
      return { ...state, accountNotFound: payload.accountNotFound, accountHasError: payload.accountHasError };

    case 'SET_IS_VISIBLE_DELETE_CONFIRM_DIALOG':
      return { ...state, isDeleteDialogVisible: payload.isDeleteDialogVisible };

    case 'TOGGLE_DELETING_USER':
      return { ...state, isUserDeleting: payload.isDeleting };

    default:
      return state;
  }
};
