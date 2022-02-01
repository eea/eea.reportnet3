export const manageManualAcceptanceDatasetReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INITIAL_DATASET_MESSAGE':
      return { ...state, initialDatasetMessage: payload.value };

    case 'ON_CHANGE_STATUS':
      return { ...state, datasetFeedbackStatus: payload.value };

    case 'ON_UPDATE_MESSAGE':
      return { ...state, datasetMessage: payload.message };

    case 'SET_IS_AUTOMATIC_DELETION_VISIBLE':
      return { ...state, isAutomaticReportingDeletionVisible: payload };

    default:
      return state;
  }
};
