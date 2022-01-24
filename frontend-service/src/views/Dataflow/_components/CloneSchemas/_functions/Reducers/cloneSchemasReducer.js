export const cloneSchemasReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_LOAD':
      return { ...state, ...payload };

    case 'IS_LOADING':
      return { ...state, isLoading: payload.value };

    case 'ON_PAGINATE':
      return { ...state, pagination: payload.pagination };

    case 'ON_SELECT_DATAFLOW':
      return { ...state, chosenDataflow: { ...state.chosenDataflow, id: payload.id, name: payload.name } };

    default:
      return state;
  }
};
