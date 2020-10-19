export const validationReducer = (state, { type, payload }) => {
  switch (type) {
    case 'SET_TOTALS_ERRORS':
      return {
        ...state,
        totalFilteredRecords: payload.totalFilteredRecords,
        totalRecords: payload.totalRecords
      };

    case 'SET_TOTAL_GROUPED_ERRORS':
      return {
        ...state,
        totalErrors: payload.totalErrors,
        totalFilteredGroupedRecords: payload.totalFilteredGroupedRecords
      };

    default:
      return state;
  }
};
