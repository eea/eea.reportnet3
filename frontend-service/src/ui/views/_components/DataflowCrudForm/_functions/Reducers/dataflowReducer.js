export const dataflowReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ON_INIT_DATA':
      return payload;

    case 'ON_SELECT_DATAFLOW':
      return {
        ...state,
        selectedDataflow: state[payload],
        selectedDataflowId: payload
      };

    case 'ON_EDIT_DATAFLOW':
      return {
        ...state,
        [payload.id]: { name: payload.name, description: payload.description, id: payload.id },
        selectedDataflow: { name: payload.name, description: payload.description, id: payload.id }
      };

    case 'ON_RESET_DATAFLOW_DATA':
      delete state.selectedDataflow;
      delete state.selectedDataflowId;
      return {
        ...state
      };

    default:
      return {
        ...state
      };
  }
};
