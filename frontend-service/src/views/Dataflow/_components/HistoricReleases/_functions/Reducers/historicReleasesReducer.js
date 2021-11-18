export const historicReleasesReducer = (state, { type, payload }) => {
  switch (type) {
    case 'FILTERED_DATA':
      return { ...state, filteredData: payload.data };

    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'IS_FILTERED':
      return { ...state, filtered: payload.value };

    case 'GET_DATA_PROVIDER_CODES':
      return { ...state, dataProviderCodes: payload.dataProviderCodes };

    case 'IS_LOADING':
      return { ...state, isLoading: payload.value };

    default:
      return state;
  }
};
