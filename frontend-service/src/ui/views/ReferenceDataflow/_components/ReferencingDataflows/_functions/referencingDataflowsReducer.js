export const referencingDataflowsReducer = (state, { type, payload }) => {
  switch (type) {
    case 'LOADING_ERROR': {
      return { ...state, requestStatus: 'rejected', error: payload.error };
    }

    case 'LOADING_STARTED': {
      return { ...state, requestStatus: 'pending' };
    }

    case 'LOADING_SUCCESS': {
      return { ...state, requestStatus: 'resolved', dataflows: payload.dataflows };
    }

    case 'ON_LOAD_FILTERED_DATA':
      return { ...state, filteredData: payload.dataflows };

    case 'ON_PAGINATE':
      return { ...state, pagination: payload.pagination };

    default: {
      throw new Error(`Unhandled action type: ${type}`);
    }
  }
};
