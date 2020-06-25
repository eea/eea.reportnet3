export const cloneSchemasReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'FILTERED_DATA':
      return { ...state, filteredData: payload.data };

    case 'IS_LOADING':
      return { ...state, isLoading: payload.value };

    case 'ON_PAGINATE':
      return { ...state, pagination: payload.pagination };

    case 'ON_SELECT_DATAFLOW':
      return { ...state, chosenDataflow: payload.rowData, chosenDataflowId: payload.rowData.id };

    case 'ON_TOGGLE_VIEW':
      return { ...state, isTableView: payload.view };
    default:
      return state;
  }
};
