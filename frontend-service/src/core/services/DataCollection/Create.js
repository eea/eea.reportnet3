export const Create = ({ dataCollectionRepository }) => async (
  dataflowId,
  schemaId,
  dataCollectionName,
  creationDate
) => dataCollectionRepository.create(dataflowId, schemaId, dataCollectionName, creationDate);
