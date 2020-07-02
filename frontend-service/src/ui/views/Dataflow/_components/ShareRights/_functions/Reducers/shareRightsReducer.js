export const shareRightsReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ADD_CONTRIBUTOR':
      return { ...state, account: payload.email };

    case 'GET_ALL_CONTRIBUTORS':
      return { ...state, contributors: payload.contributors };

    case 'ON_UPDATE_DATA':
      return { ...state, isDataUpdated: payload };

    case 'TOGGLE_DELETE_CONFIRM_DIALOG':
      return { ...state, isDeleteDialogVisible: payload.isVisible };

    default:
      return state;
  }
};
