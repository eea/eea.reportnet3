export const shareRightsReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ADD_CONTRIBUTOR':
      return { ...state, account: payload.email };

    case 'GET_ALL_CONTRIBUTORS':
      return { ...state, contributors: payload.contributors };

    case 'ON_DATA_CHANGE':
      return { ...state, isDataUpdated: payload.isDataUpdated };

    case 'ON_DELETE_CONTRIBUTOR':
      return {
        ...state,
        isDeleteDialogVisible: payload.isDeleteDialogVisible,
        contributorAccountToDelete: payload.contributorAccountToDelete
      };

    case 'ON_SET_ACCOUNT':
      return { ...state, contributors: payload.contributors, accountHasError: payload.accountHasError };

    case 'ON_WRITE_PERMISSION_CHANGE':
      return { ...state, contributors: payload.contributors };

    case 'SET_ACCOUNT_HAS_ERROR':
      return { ...state, accountHasError: payload.accountHasError };

    case 'SET_IS_VISIBLE_DELETE_CONFIRM_DIALOG':
      return { ...state, isDeleteDialogVisible: payload.isDeleteDialogVisible };

    default:
      return state;
  }
};
