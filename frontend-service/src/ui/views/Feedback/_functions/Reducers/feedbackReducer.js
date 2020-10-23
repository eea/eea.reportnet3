export const feedbackReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ON_SEND_MESSAGE':
      const inmMessages = [...state.messages];
      inmMessages.push(payload.value);
      return {
        ...state,
        messages: inmMessages,
        messageToSend: ''
      };
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
      console.log(payload);
      return {
        ...state,
        messages: [...state.messages, ...payload],
        isLoading: false
      };
    case 'ON_UPDATE_MESSAGE':
      return {
        ...state,
        messageToSend: payload.value
      };
    default:
      return state;
  }
};
