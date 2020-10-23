export const listMessagesReducer = (state, { type, payload }) => {
  switch (type) {
    case 'SET_IS_LOADING':
      return {
        ...state,
        isLoadingNewMessages: payload
      };

    default:
      return state;
  }
};
