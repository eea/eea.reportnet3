export const GetAllSchemas = ({ dataflowRepository }) => async dataflowId =>
  dataflowRepository.getAllSchemas(dataflowId);
