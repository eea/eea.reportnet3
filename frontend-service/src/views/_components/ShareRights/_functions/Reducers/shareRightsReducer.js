export const shareRightsReducer = (state, { type, payload }) => {
  switch (type) {
    case 'GET_USER_RIGHT_LIST':
      return { ...state, userRightList: payload.userRightList, clonedUserRightList: payload.clonedUserRightList };

    case 'ON_CLOSE_MANAGEMENT_DIALOG':
      return { ...state, userRight: { account: '', isNew: true, role: '' }, isEditingModal: false };

    case 'ON_DATA_CHANGE':
      return { ...state, dataUpdatedCount: state.dataUpdatedCount + 1 };

    case 'ON_DELETE_USER_RIGHT':
      return {
        ...state,
        isDeleteDialogVisible: payload.isDeleteDialogVisible,
        userRightToDelete: payload.userRightToDelete
      };

    case 'ON_EDIT_USER_RIGHT':
      return { ...state, isEditingModal: payload.isEditingModal, userRight: payload.userRight };

    case 'ON_PAGINATE':
      return { ...state, pagination: payload.pagination };

    case 'ON_ROLE_CHANGE':
      return { ...state, userRight: { ...state.userRight, role: payload.role } };

    case 'ON_SET_ACCOUNT':
      return {
        ...state,
        userRight: { ...state.userRight, account: payload.account },
        accountHasError: payload.accountHasError,
        accountNotFound: payload.accountNotFound
      };

    case 'SET_ACCOUNT_HAS_ERROR':
      return { ...state, accountHasError: payload.accountHasError };

    case 'SET_ACCOUNT_NOT_FOUND':
      return { ...state, accountNotFound: payload.accountNotFound, accountHasError: payload.accountHasError };

    case 'SET_IS_LOADING':
      return {
        ...state,
        loadingStatus: {
          ...state.loadingStatus,
          isActionButtonsLoading: payload.isActionButtonsLoading,
          isInitialLoading: payload.isInitialLoading
        }
      };

    case 'SET_IS_VISIBLE_DELETE_CONFIRM_DIALOG':
      return { ...state, isDeleteDialogVisible: payload.isDeleteDialogVisible };

    case 'TOGGLE_DELETING_USER_RIGHT':
      return { ...state, isDeletingUserRight: payload.isDeleting };

    case 'SET_IS_LOADING_BUTTON':
      return { ...state, isLoadingButton: payload.isLoadingButton };

    case 'SET_USER_RIGHT_ID':
      return { ...state, actionsButtons: { ...state.actionsButtons, id: payload.id } };

    case 'SET_ACTIONS':
      return {
        ...state,
        actionsButtons: { ...state.actionsButtons, isDeleting: payload.isDeleting, isEditing: payload.isEditing }
      };

    case 'ON_RESET_ALL':
      return {
        ...state,
        accountHasError: false,
        actionsButtons: { id: null, isEditing: false, isDeleting: false },
        loadingStatus: { isActionButtonsLoading: false, isInitialLoading: false },
        userRight: { account: '', isNew: true, role: '' },
        userRightToDelete: {}
      };

    default:
      return state;
  }
};
