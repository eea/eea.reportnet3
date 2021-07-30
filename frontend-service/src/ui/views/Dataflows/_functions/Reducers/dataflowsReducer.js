export const dataflowsReducer = (state, { type, payload }) => {
  switch (type) {
    case 'SET_DATAFLOWS':
      return { ...state, [payload.type]: payload.data };

    case 'HAS_PERMISSION':
      return {
        ...state,
        isAdmin: payload.isAdmin,
        isCustodian: payload.isCustodian,
        isNationalCoordinator: payload.isNationalCoordinator
      };

    case 'MANAGE_DIALOGS':
      return { ...state, [payload.dialog]: payload.value };

    case 'ON_CHANGE_TAB':
      return { ...state, activeIndex: payload.index };

    case 'SET_LOADING':
      return { ...state, loadingStatus: { ...state.loadingStatus, [payload.tab]: payload.status } };

    default:
      return state;
  }
};
