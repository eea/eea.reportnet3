export const referencingDataflowsReducer = (state, { type, payload }) => {
  switch (type) {
    case 'LOADING_STARTED': {
      return { ...state, requestStatus: 'pending' };
    }

    case 'LOADING_SUCCESS': {
      return { ...state, requestStatus: 'resolved', dataflows: payload.dataflows };
    }

    case 'ON_PAGINATE':
      return { ...state, pagination: payload.pagination };

    default: {
      throw new Error(`Unhandled action type: ${type}`);
    }
  }
};
