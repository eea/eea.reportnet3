export const GetErrorPosition = ({ dataSetRepository }) => async (objectId, dataSetId, entityType) =>
  dataSetRepository.errorPositionByObjectId(objectId, dataSetId, entityType);
