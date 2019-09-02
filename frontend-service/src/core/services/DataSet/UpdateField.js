export const UpdateField = ({ dataSetRepository }) => async (
  dataSetId,
  fieldSchemaId,
  fieldId,
  fieldType,
  fieldValue
) => dataSetRepository.updateFieldById(dataSetId, fieldSchemaId, fieldId, fieldType, fieldValue);
