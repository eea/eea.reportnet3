export const GetReferencedFieldValues = ({ datasetRepository }) => async (
  datasetId,
  fieldSchemaId,
  idFk,
  searchToken
) => datasetRepository.getReferencedFieldValues(datasetId, fieldSchemaId, idFk, searchToken);
