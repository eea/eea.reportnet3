export const webformTableReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'IS_LOADING':
      return { ...state, isLoading: payload.value };

    case 'ON_LOAD_DATA':
      return { ...state, webformData: { ...state.webformData, elementsRecords: payload.records } };

    case 'ON_UPDATE_DATA':
      return { ...state, isDataUpdated: payload.value };

    case 'SET_IS_ADDING_MULTIPLE':
      return {
        ...state,
        isAddingMultiple: payload.isAddingMultiple,
        addingOnTableSchemaId: payload.addingOnTableSchemaId
      };

    default:
      return state;
  }
};
