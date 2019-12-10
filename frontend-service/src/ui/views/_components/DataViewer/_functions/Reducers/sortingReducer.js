export const sortingReducer = (state, { type, payload }) => {
  switch (type) {
    case 'SORT_TABLE':
      return {
        ...state,
        sortOrder: payload.order,
        sortField: payload.field
      };

    default:
      return state;
  }
};
