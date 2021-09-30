export const dataflowsReducer = (state, { type, payload }) => {
  switch (type) {
    case 'SET_DATAFLOWS':
      return {
        ...state,
        [payload.type]: payload.data,
        dataflowsCountFirstLoad: false,
        dataflowsCount: { ...state.dataflowsCount, [payload.type]: payload.data.length }
      };

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

    case 'SET_DATAFLOWS_COUNT': {
      return {
        ...state,
        dataflowsCount: payload,
        dataflowsCountFirstLoad: true,
        activeIndex: state.activeIndex !== 2 ? (payload.reporting > 0 ? 0 : 1) : state.activeIndex
      };
    }

    case 'UPDATE_DATAFLOWS_COUNT': {
      return {
        ...state,
        dataflowsCount: { ...state.dataflowsCount, [payload]: state.dataflowsCount[payload] + 1 }
      };
    }

    case 'SET_LOADING':
      return { ...state, loadingStatus: { ...state.loadingStatus, [payload.tab]: payload.status } };

    case 'ON_LOAD_OBLIGATION':
      return { ...state, obligation: { id: payload.id, title: payload.title } };

    default:
      return state;
  }
};
