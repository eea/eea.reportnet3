import { apiValidation } from 'core/infrastructure/api/domain/model/Validation';
import { Validation } from 'core/domain/model/Validation/Validation';

const deleteById = async (datasetSchemaId, ruleId) => {
  return await apiValidation.deleteById(datasetSchemaId, ruleId);
};

const getAll = async datasetSchemaId => {
  const validationsDTO = await apiValidation.getAll(datasetSchemaId);
  return validationsDTO.map(validationsDTO => {
    return new Validation({
      id: validationsDTO.id,
      levelError: validationsDTO.levelError,
      entityType: validationsDTO.entityType,
      date: validationsDTO.date,
      message: validationsDTO.message
    });
  });
};

export const ApiValidationRepository = {
  deleteById,
  getAll
};
