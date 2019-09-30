export const GetErrorPosition = ({ datasetRepository }) => async (objectId, datasetId, entityType) =>
  datasetRepository.errorPositionByObjectId(objectId, datasetId, entityType);
