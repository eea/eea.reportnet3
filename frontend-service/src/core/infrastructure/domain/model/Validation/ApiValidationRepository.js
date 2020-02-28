import { isEmpty, isNull, isUndefined } from 'lodash';

import { apiValidation } from 'core/infrastructure/api/domain/model/Validation';
import { Validation } from 'core/domain/model/Validation/Validation';

const create = async (datasetSchemaId, validation) => {
  return await apiValidation.create(datasetSchemaId, validation);
};

const deleteById = async (datasetSchemaId, ruleId) => {
  return await apiValidation.deleteById(datasetSchemaId, ruleId);
};

const getAll = async datasetSchemaId => {
  const validationsListDTO = await apiValidation.getAll(datasetSchemaId);
  if (isUndefined(validationsListDTO) || isEmpty(validationsListDTO.rules)) {
    return;
  }

  const validationsList = {};
  validationsList.datasetSchemaId = validationsListDTO.idDatasetSchema;
  validationsList.rulesSchemaId = validationsListDTO.rulesSchemaId;

  const validationsData = parseDataValidationRulesDTO(validationsListDTO.rules);
  validationsList.entityTypes = validationsData.entityTypes;
  validationsList.validations = validationsData.validations;
  return validationsList;
};

const parseDataValidationRulesDTO = validations => {
  const validationsData = {};
  const entityTypes = [];

  validationsData.validations = validations.map(validationDTO => {
    entityTypes.push(validationDTO.type);
    return new Validation({
      activationGroup: validationDTO.activationGroup,
      automatic: validationDTO.automatic,
      condition: validationDTO.whenCondition,
      date: validationDTO.activationGroup,
      description: validationDTO.description,
      enabled: validationDTO.enabled,
      enabled: validationDTO.enabled,
      entityType: validationDTO.type,
      id: validationDTO.ruleId,
      levelError:
        !isUndefined(validationDTO.thenCondition) && !isNull(validationDTO.thenCondition[1])
          ? validationDTO.thenCondition[1]
          : null,
      message:
        !isUndefined(validationDTO.thenCondition) && !isNull(validationDTO.thenCondition[0])
          ? validationDTO.thenCondition[0]
          : null,
      name: validationDTO.ruleName,
      referenceId: validationDTO.referenceId,
      shortCode: validationDTO.shortCode
    });
  });

  validationsData.entityTypes = [...new Set(entityTypes)];
  return validationsData;
};

const translateRules = rule => {
  return rule;
};

const validationToTransfer = validation => {
  const whenCondition = translateRules(validation.rules);

  return {
    description: validation.description,
    ensabled: validation.active,
    referenceId: validation.fieldId,
    ruleName: '',
    shortCode: validation.shortCode,
    type: 'FIELD',
    thenCondition: [validation.errorMessage, validation.errorLevel],
    whenCondition: {
      whenCondition
    }
  };
};
export const ApiValidationRepository = {
  create,
  deleteById,
  getAll
};
