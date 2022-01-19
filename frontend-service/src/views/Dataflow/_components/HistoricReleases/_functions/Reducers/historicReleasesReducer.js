export const historicReleasesReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'GET_DATA_PROVIDER_CODES':
      return { ...state, dataProviderCodes: payload.dataProviderCodes };

    case 'IS_LOADING':
      return { ...state, isLoading: payload.value };

    default:
      return state;
  }
};
