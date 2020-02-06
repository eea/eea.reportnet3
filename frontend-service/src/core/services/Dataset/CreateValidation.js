export const CreateValidation = ({ datasetRepository }) => (entityType, id, levelError, message) =>
  datasetRepository.createValidation(entityType, id, levelError, message);
