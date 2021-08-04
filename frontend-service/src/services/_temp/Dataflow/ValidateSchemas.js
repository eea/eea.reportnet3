export const ValidateSchemas = ({ dataflowRepository }) => async dataflowId =>
  dataflowRepository.schemasValidation(dataflowId);
