export const GetReferencedFieldValues = ({ datasetRepository }) => async (datasetId, fieldSchemaId, searchToken) =>
  datasetRepository.getReferencedFieldValues(datasetId, fieldSchemaId, searchToken);
