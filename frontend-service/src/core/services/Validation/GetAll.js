export const GetAll = ({ validationRepository }) => async (datasetSchemaId, reporting) =>
  validationRepository.getAll(datasetSchemaId, reporting);
