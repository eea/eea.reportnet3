export const ReleaseDataflow = ({ snapshotRepository }) => async (dataflowId, dataProviderId, restrictFromPublic) =>
  snapshotRepository.releaseDataflow(dataflowId, dataProviderId, restrictFromPublic);
