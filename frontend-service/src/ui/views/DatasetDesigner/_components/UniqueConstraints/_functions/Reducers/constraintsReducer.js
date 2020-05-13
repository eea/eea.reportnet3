export const constraintsReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'FILTERED_DATA':
      return { ...state, filteredData: payload.data };

    default:
      return state;
  }
};
