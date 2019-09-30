export const UpdateField = ({ datasetRepository }) => async (
  datasetId,
  fieldSchemaId,
  fieldId,
  fieldType,
  fieldValue
) => datasetRepository.updateFieldById(datasetId, fieldSchemaId, fieldId, fieldType, fieldValue);
