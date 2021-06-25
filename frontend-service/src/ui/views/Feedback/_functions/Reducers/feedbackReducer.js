import isNil from 'lodash/isNil';
import uniqBy from 'lodash/uniqBy';

export const feedbackReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ON_LOAD_MORE_MESSAGES':
      let inmAllMessages = [];
      if (payload.length !== state.messages.length) {
        inmAllMessages = [...payload, ...state.messages];
      } else {
        inmAllMessages = [...state.messages];
      }
      return {
        ...state,
        currentPage: payload.length === 50 ? state.currentPage + 1 : state.currentPage,
        messages: uniqBy(inmAllMessages, 'id'),
        newMessageAdded: false
      };
    case 'ON_SEND_MESSAGE':
      const inmMessages = [...state.messages];
      inmMessages.push(payload.value);
      return {
        ...state,
        messages: inmMessages,
        messageToSend: '',
        newMessageAdded: true
      };
    case 'SET_DATAFLOW_NAME':
      return {
        ...state,
        dataflowName: payload
      };
    case 'SET_DATAPROVIDERS':
      return { ...state, dataProviders: payload };
    case 'SET_IS_CUSTODIAN':
      return {
        ...state,
        isCustodian: payload
      };
    case 'SET_IS_LOADING':
      return {
        ...state,
        isLoading: payload
      };
    case 'SET_IS_SENDING':
      return {
        ...state,
        isSending: payload
      };
    case 'SET_IS_VISIBLE_DIALOG':
      return { ...state, isDialogVisible: payload };
    case 'SET_MESSAGE_TO_SHOW':
      return { ...state, isDialogVisible: true, messageToShow: payload };
    case 'SET_MESSAGES':
      return {
        ...state,
        currentPage: payload.length === 50 ? 1 : 0,
        messages: payload,
        isLoading: false
      };
    case 'SET_SELECTED_DATAPROVIDER':
      return {
        ...state,
        selectedDataProvider: payload,
        currentPage: !isNil(payload) && !isNil(payload.currentPage) ? state.currentPage : 0
      };
    case 'ON_UPDATE_MESSAGE':
      return {
        ...state,
        messageToSend: payload.value
      };
    case 'ON_UPDATE_MESSAGE_FIRST_LOAD':
      return {
        ...state,
        messageFirstLoad: payload
      };
    case 'ON_UPDATE_NEW_MESSAGE_ADDED':
      return {
        ...state,
        newMessageAdded: payload
      };
    case 'RESET_MESSAGES':
      return { ...state, messages: payload };
    default:
      return state;
  }
};
