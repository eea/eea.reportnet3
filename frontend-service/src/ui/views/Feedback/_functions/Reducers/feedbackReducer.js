export const feedbackReducer = (state, { type, payload }) => {
  switch (type) {
    case 'SET_DATAFLOW_NAME':
      return {
        ...state,
        dataflowName: payload
      };
    case 'SET_IS_LOADING':
      return {
        ...state,
        isLoading: payload
      };
    case 'SET_IS_VISIBLE_DIALOG':
      return { ...state, isDialogVisible: payload };
    case 'SET_MESSAGE_TO_SHOW':
      console.log(payload);
      return { ...state, isDialogVisible: true, messageToShow: payload };
    case 'SET_MESSAGES':
      return {
        ...state,
        messages: payload,
        isLoading: false
      };

    default:
      return state;
  }
};
