export const GetFileExtensions = ({ datasetRepository }) => async datasetSchemaId =>
  datasetRepository.getFileExtensions(datasetSchemaId);
