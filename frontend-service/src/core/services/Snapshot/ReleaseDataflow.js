export const ReleaseDataflow = ({ snapshotRepository }) => async (dataflowId, dataProviderId) =>
  snapshotRepository.releaseDataflow(dataflowId, dataProviderId);
