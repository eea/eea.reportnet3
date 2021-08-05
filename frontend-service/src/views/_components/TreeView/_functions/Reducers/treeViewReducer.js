export const treeViewReducer = (state, { type, payload }) => {
  switch (type) {
    case 'SET_FILTER':
      return {
        ...state,
        filters: { ...state.filters, [payload.field]: payload.value }
      };

    default:
      return state;
  }
};
