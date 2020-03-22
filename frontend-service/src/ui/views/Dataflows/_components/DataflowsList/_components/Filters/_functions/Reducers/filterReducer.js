export const filterReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ORDER_DATA':
      return {
        ...state,
        data: payload.sortedData,
        filteredData: payload.filteredSortedData,
        orderBy: { ...state.orderBy, [payload.property]: -payload.orderBy }
      };

    case 'FILTER_DATA':
      return {
        ...state,
        filterBy: { ...state.filterBy, [payload.filter]: payload.value },
        filteredData: payload.filteredData
      };

    case 'CLEAR_ALL':
      return {
        ...state,
        filterBy: payload.filterBy,
        filteredData: payload.filteredData,
        labelAnimations: payload.labelAnimations,
        orderBy: payload.orderBy
      };

    case 'ANIMATE_LABEL':
      return {
        ...state,
        labelAnimations: { ...state.labelAnimations, [payload.animatedProperty]: payload.isAnimated }
      };

    default:
      return state;
  }
};
