export const GetWebFormData = ({ dataSetRepository }) => async (dataSetId, tableSchemaId) =>
  dataSetRepository.webFormDataById(dataSetId, tableSchemaId);
