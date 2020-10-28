export const listMessagesReducer = (state, { type, payload }) => {
  switch (type) {
    case 'SET_IS_LOADING':
      return {
        ...state,
        isLoadingNewMessages: payload
      };
    case 'SET_SEPARATOR_INDEX':
      console.log(payload);
      return {
        ...state,
        separatorIndex: payload
      };
    default:
      return state;
  }
};
