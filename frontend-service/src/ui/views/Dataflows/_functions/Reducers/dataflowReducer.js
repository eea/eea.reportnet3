export const dataflowReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ON_SELECT_DATAFLOW':
      return { ...state, selectedDataflow: state[payload], selectedDataflowId: payload };

    default:
      return {
        ...state
      };
  }
};
