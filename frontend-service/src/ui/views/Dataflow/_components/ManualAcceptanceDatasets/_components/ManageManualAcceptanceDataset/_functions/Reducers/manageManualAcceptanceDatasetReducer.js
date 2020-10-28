export const manageManualAcceptanceDatasetReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_DATASET_MESSAGE':
      return { ...state, initialDatasetMessage: payload.value };

    case 'ON_CHANGE_STATUS':
      return { ...state, datasetFeedbackStatus: payload.value };

    case 'ON_UPDATE_MESSAGE':
      return { ...state, datasetMessage: payload.message, updateButtonEnabled: payload.value };

    case 'UPDATE_BUTTON_DISABLED':
      return { ...state, updateButtonEnabled: payload.value };

    default:
      return state;
  }
};
