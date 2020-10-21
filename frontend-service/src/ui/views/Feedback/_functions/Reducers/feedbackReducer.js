export const feedbackReducer = (state, { type, payload }) => {
  switch (type) {
    case 'SET_DATAFLOW_NAME':
      return {
        ...state,
        dataflowName: payload
      };

    default:
      return state;
  }
};
