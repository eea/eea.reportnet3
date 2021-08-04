export const UpdateField = ({ datasetRepository }) => async (
  datasetId,
  fieldSchemaId,
  fieldId,
  fieldType,
  fieldValue,
  updateInCascade
) => datasetRepository.updateFieldById(datasetId, fieldSchemaId, fieldId, fieldType, fieldValue, updateInCascade);
