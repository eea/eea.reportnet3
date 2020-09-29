export const bigButtonListRepresentativeReducer = (state, { type, payload }) => {
  switch (type) {
    case 'GET_HISTORIC_RELEASE_DATASET_DATA':
      return {
        ...state,
        datasetId: payload.datasetId,
        historicReleasesDialogHeader: payload.value,
        dataProviderId: payload.dataProviderId
      };

    case 'ON_SHOW_HISTORIC_RELEASES':
      return { ...state, historicReleasesView: payload.typeView, isHistoricReleasesDialogVisible: payload.value };

    case 'ON_CLOSE_HISTORIC_RELEASE_DIALOG':
      return { ...state, isHistoricReleasesDialogVisible: payload.value };

    default:
      return state;
  }
};
