export const customFileUploadReducer = (state, { type, payload }) => {
  switch (type) {
    case 'UPLOAD_PROPERTY':
      return {
        ...state,
        ...payload
      };

    default:
      return state;
  }
};
