export const cloneSchemasReducer = (state, { type, payload }) => {
  switch (type) {
    case 'FILTER_DATA':
      return { ...state, filteredData: payload.filteredData };

    case 'IS_LOADING':
      return { ...state, isLoading: payload.value };

    case 'ON_TOGGLE_VIEW':
      return { ...state, isTableView: payload.view };
    default:
      return state;
  }
};
