export const GetReferencedFieldValues = ({ datasetRepository }) => async (
  datasetId,
  fieldSchemaId,
  searchToken,
  conditionalValue,
  datasetSchemaId
) =>
  datasetRepository.getReferencedFieldValues(datasetId, fieldSchemaId, searchToken, conditionalValue, datasetSchemaId);
