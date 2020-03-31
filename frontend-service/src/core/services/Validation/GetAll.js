export const GetAll = ({ validationRepository }) => async datasetSchemaId =>
  validationRepository.getAll(datasetSchemaId);
