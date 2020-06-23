export const cloneSchemasReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'FILTERED_DATA':
      return { ...state, filteredData: payload.data };

    case 'IS_LOADING':
      return { ...state, isLoading: payload.value };

    case 'ON_TOGGLE_VIEW':
      return { ...state, isTableView: payload.view };
    default:
      return state;
  }
};
