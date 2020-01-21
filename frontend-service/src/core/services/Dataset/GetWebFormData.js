export const GetWebFormData = ({ datasetRepository }) => async (datasetId, tableSchemaId) =>
  datasetRepository.webFormDataById(datasetId, tableSchemaId);
