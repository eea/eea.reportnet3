export const GetReferencedFieldValues = ({ datasetRepository }) => async (
  datasetId,
  fieldSchemaId,
  searchToken,
  conditionalValue,
  datasetSchemaId,
  resultsNumber
) =>
  datasetRepository.getReferencedFieldValues(
    datasetId,
    fieldSchemaId,
    searchToken,
    conditionalValue,
    datasetSchemaId,
    resultsNumber
  );
