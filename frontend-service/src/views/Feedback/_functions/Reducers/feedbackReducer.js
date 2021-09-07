import isNil from 'lodash/isNil';
import uniqBy from 'lodash/uniqBy';

export const feedbackReducer = (state, { type, payload }) => {
  switch (type) {
    case 'ON_DELETE_MESSAGE':
      return { ...state, messages: state.messages.filter(message => message.id !== payload) };
    case 'ON_LOAD_MORE_MESSAGES':
      let inmAllMessages = [];
      console.log(payload.length, state.messages.length, state.currentPage * 50);
      if (payload.length !== state.messages.length || state.messages.length >= state.currentPage * 50) {
        inmAllMessages = [...payload, ...state.messages];
      } else {
        inmAllMessages = [...state.messages];
      }
      return {
        ...state,
        currentPage: payload.length === 50 ? state.currentPage + 1 : state.currentPage,
        messages: uniqBy(inmAllMessages, 'id'),
        moreMessagesLoaded: true,
        moreMessagesLoading: false,
        newMessageAdded: false
      };
    case 'ON_SEND_ATTACHMENT':
      const inmAttachMessages = [...state.messages];
      inmAttachMessages.push(payload);
      return {
        ...state,
        messages: inmAttachMessages,
        newMessageAdded: true,
        importFileDialogVisible: false,
        draggedFiles: null
      };
    case 'ON_TOGGLE_LAZY_LOADING':
      return { ...state, moreMessagesLoading: true };
    case 'ON_SEND_MESSAGE':
      const inmMessages = [...state.messages];
      inmMessages.push(payload.value);
      return {
        ...state,
        messages: inmMessages,
        messageToSend: '',
        newMessageAdded: true
      };
    case 'SET_DATAFLOW_DETAILS':
      return {
        ...state,
        dataflowName: payload.dataflowName,
        isBusinessDataflow: payload.isBusinessDataflow
      };
    case 'SET_DATAPROVIDERS':
      return { ...state, dataProviders: payload };
    case 'SET_PERMISSIONS':
      return {
        ...state,
        isAdmin: payload.isAdmin,
        isCustodian: payload.isCustodian
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
        moreMessagesLoaded: false,
        selectedDataProvider: payload,
        currentPage: !isNil(payload) && !isNil(payload.currentPage) ? state.currentPage : 0
      };
    case 'SET_DRAGGED_FILES':
      return { ...state, draggedFiles: payload, importFileDialogVisible: true };
    case 'RESET_DRAGGED_FILES':
      return { ...state, draggedFiles: null };
    case 'TOGGLE_FILE_UPLOAD_VISIBILITY':
      return {
        ...state,
        importFileDialogVisible: payload,
        draggedFiles: !payload ? null : state.draggedFiles
      };
    case 'TOGGLE_IS_DRAGGING':
      return { ...state, isDragging: payload };
    case 'ON_UPDATE_MESSAGE':
      return {
        ...state,
        messageToSend: payload.value
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
