export const reducer = (state, { type, payload }) => {
  switch (type) {
    case 'REFRESH':
      return {
        ...state,
        refresher: !state.refresher
      };

    case 'DELETE_CONTRIBUTOR':
      return {
        ...state,
        contributors: payload.updatedList,
        isVisibleConfirmDeleteDialog: false,
        refresher: !state.refresher
      };

    case 'MANAGE_ERRORS':
      return { ...state, contributorsHaveError: payload.contributorsHaveError };

    case 'HIDE_CONFIRM_DIALOG':
      return {
        ...state,
        isVisibleConfirmDeleteDialog: false,
        accountToDelete: ''
      };

    case 'INITIAL_LOAD':
      return {
        ...state,
        contributors: payload.contributors,
        initialContributors: payload.contributorsByCopy,
        contributorsHaveError: []
      };

    case 'ON_ACCOUNT_CHANGE':
      return {
        ...state,
        contributors: payload.contributors
      };

    case 'ON_PERMISSIONS_CHANGE':
      return {
        ...state,
        contributors: payload.contributors
      };

    case 'SHOW_CONFIRM_DIALOG':
      return {
        ...state,
        isVisibleConfirmDeleteDialog: true,
        contributorToDelete: payload
      };

    default:
      return state;
  }
};
