export const listMessagesReducer = (state, { type, payload }) => {
  switch (type) {
    case 'SET_IS_LOADING':
      return {
        ...state,
        isLoadingNewMessages: payload
      };
    case 'SET_SEPARATOR_INDEX':
      return {
        ...state,
        separatorIndex: payload
      };
    case 'SET_LIST_CONTENT':
      return {
        ...state,
        listContent: payload
      };
    case 'UPDATE_SCROLL_STATES':
      return {
        ...state,
        resetScrollStates: payload
      };
    default:
      return state;
  }
};
