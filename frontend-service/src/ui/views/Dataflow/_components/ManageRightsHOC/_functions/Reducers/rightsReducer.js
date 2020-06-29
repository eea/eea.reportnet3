export const reducer = (state, { type, payload }) => {
  switch (type) {
    case 'REFRESH':
      return {
        ...state,
        refresher: !state.refresher
      };

    case 'DELETE_REPRESENTATIVE':
      return {
        ...state,
        representatives: payload.updatedList,
        refresher: !state.refresher,
        isVisibleConfirmDeleteDialog: false
      };

    case 'MANAGE_ERRORS':
      return { ...state, representativesHaveError: payload.representativesHaveError };

    case 'HIDE_CONFIRM_DIALOG':
      return {
        ...state,
        isVisibleConfirmDeleteDialog: false,
        representativeIdToDelete: ''
      };

    case 'INITIAL_LOAD':
      return {
        ...state,
        representatives: payload.representatives,
        initialRepresentatives: payload.representativesByCopy,
        representativesHaveError: []
      };

    case 'ON_ACCOUNT_CHANGE':
      return {
        ...state,
        representatives: payload.representatives
      };

    case 'ON_PERMISSIONS_CHANGE':
      return {
        ...state,
        representatives: payload.representatives
      };

    case 'SHOW_CONFIRM_DIALOG':
      return {
        ...state,
        isVisibleConfirmDeleteDialog: true,
        representativeIdToDelete: payload.representativeId
      };

    default:
      return state;
  }
};
