import isNil from 'lodash/isNil';

export const dataflowsReducer = (state, { type, payload }) => {
  switch (type) {
    case 'SET_DATAFLOWS':
      const getIndex = () => {
        let idx = -1;
        if (state.dataflowsCount.reporting > 0) {
          idx = 0;
        } else if (state.dataflowsCount.business > 0) {
          idx = 1;
        } else if (state.dataflowsCount.citizenScience > 0) {
          idx = 2;
        } else {
          idx = 0;
        }
        return idx;
      };

      return {
        ...state,
        [payload.type]: payload.data,
        dataflowsCountFirstLoad: false,
        dataflowsCount: { ...state.dataflowsCount, [payload.type]: payload.data.length },
        activeIndex:
          isNil(payload.contextCurrentDataflowType) && state.dataflowsCountFirstLoad ? getIndex() : state.activeIndex
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
        dataflowsCountFirstLoad: true
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

    case 'ON_PAGINATE':
      return { ...state, pagination: payload.pagination };

    case 'SET_IS_VALIDATING_ALL_DATAFLOWS_USERS':
      return { ...state, isValidatingAllDataflowsUsers: payload.isValidatingAllDataflowsUsers };

    case 'SET_PINNED_INDEX':
      return { ...state, pinnedSeparatorIndex: payload.index };

    case 'SET_GO_TO_PAGE':
      return { ...state, goToPage: payload };

    case 'SET_PAGE_INPUT_TOOLTIP':
      return { ...state, pageInputTooltip: payload };

    default:
      return state;
  }
};
