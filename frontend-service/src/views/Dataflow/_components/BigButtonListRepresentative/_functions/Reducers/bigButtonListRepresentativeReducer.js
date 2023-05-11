export const bigButtonListRepresentativeReducer = (state, { type, payload }) => {
  switch (type) {
    case 'GET_HISTORIC_RELEASE_DATASET_DATA':
      return {
        ...state,
        datasetId: payload.datasetId,
        historicReleasesDialogHeader: payload.value,
        dataProviderId: payload.dataProviderId
      };

    case 'GET_RELEASE_SNAPSHOTS_DATASET_DATA':
      return {
        ...state,
        datasetId: payload.datasetId,
        releaseSnapshotsDialogHeader: payload.value,
        dataProviderId: payload.dataProviderId
      };

    case 'ON_SHOW_HISTORIC_RELEASES':
      return { ...state, historicReleasesView: payload.typeView, isHistoricReleasesDialogVisible: payload.value };

    case 'ON_SHOW_RELEASE_SNAPSHOTS':
      return { ...state, releaseSnapshotsView: payload.typeView, isReleaseSnapshotsDialogVisible: payload.value };

    case 'ON_CLOSE_HISTORIC_RELEASE_DIALOG':
      return { ...state, isHistoricReleasesDialogVisible: payload.value };

    case 'ON_CLOSE_RELEASE_SNAPSHOTS_DIALOG':
      return { ...state, isReleaseSnapshotsDialogVisible: payload.value };

    default:
      return state;
  }
};
