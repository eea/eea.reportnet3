export const searchReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'ON_SEARCH_DATA':
      return { ...state, searchedData: payload.searchedValues, searchBy: payload.value };

    default:
      return state;
  }
};
