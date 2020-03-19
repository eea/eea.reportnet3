export const filterReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ORDER_DATA':
      return {
        ...state,
        data: payload.sortedData,
        filteredData: payload.filteredSortedData,
        orderBy: { ...state.orderBy, [payload.property]: -payload.order }
      };

    case 'FILTER_DATA':
      return {
        ...state,
        filterBy: { ...state.filterBy, [payload.filter]: payload.value },
        filteredData: payload.filteredData
      };

    case 'CLEAR_ALL_FILTERS':
      return {
        ...state,
        filterBy: payload.filterBy,
        filteredData: payload.filteredData
      };

    default:
      return state;
  }
};
