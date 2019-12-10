export const sortingReducer = (state, { type, payload }) => {
  switch (type) {
    case 'SET_TOTAL':
      return {
        ...state,
        totalRecords: payload
      };
    case 'SET_FILTERED':
      return { ...state, totalFilteredRecords: payload };

    default:
      return state;
  }
};
