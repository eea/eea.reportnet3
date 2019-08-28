export const UpdateField = ({ dataSetRepository }) => async (dataSetId, fieldSchemaId, fieldValue) =>
  dataSetRepository.updateFieldById(dataSetId, fieldSchemaId, fieldValue);
