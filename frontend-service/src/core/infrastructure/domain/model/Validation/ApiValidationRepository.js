import { capitalize, isEmpty, isNull, isUndefined } from 'lodash';

import { apiValidation } from 'core/infrastructure/api/domain/model/Validation';
import { Validation } from 'core/domain/model/Validation/Validation';

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

  const rulesData = parseDataValidationRulesDTO(validationsListDTO.rules);
  validationsList.entityTypes = rulesData.entityTypes;
  validationsList.rules = rulesData.rules;
  return validationsList;
};

const parseDataValidationRulesDTO = rulesDTO => {
  const rulesData = {};
  const entityTypes = [];

  rulesData.rules = rulesDTO.map(ruleDTO => {
    entityTypes.push(ruleDTO.type);

    const rule = {};
    rule.shortCode = ruleDTO.shortCode;
    rule.ruleId = ruleDTO.ruleId;
    rule.name = ruleDTO.ruleName;
    rule.description =
      !isUndefined(ruleDTO.thenCondition) && !isNull(ruleDTO.thenCondition[0]) ? ruleDTO.thenCondition[0] : null;
    rule.enabled = ruleDTO.enabled;
    rule.automatic = ruleDTO.automatic;
    rule.order = ruleDTO.order;
    rule.entityType = ruleDTO.type;
    rule.message =
      !isUndefined(ruleDTO.thenCondition) && !isNull(ruleDTO.thenCondition[0]) ? ruleDTO.thenCondition[0] : null;
    rule.levelError =
      !isUndefined(ruleDTO.thenCondition) && !isNull(ruleDTO.thenCondition[1]) ? ruleDTO.thenCondition[1] : null;
    return rule;
  });

  rulesData.entityTypes = [...new Set(entityTypes)];
  return rulesData;
};
export const ApiValidationRepository = {
  deleteById,
  getAll
};

const validationsDTO = () => {
  return {
    rulesSchemaId: '5e53e64200174d8528047f34',
    idDatasetSchema: '5e53e64200174d8528047f33',
    rules: [
      {
        ruleId: '5e54dc3c00174d3c7cf2bb19',
        referenceId: '5e54dbfe00174d27cc98c235',
        ruleName: 'a151704e-9069-483f-9be6-24f7efe7490c',
        automatic: true,
        enabled: true,
        salience: null,
        activationGroup: null,
        type: 'FIELD',
        whenCondition: '!isNumber(value)',
        thenCondition: ['The field must be a valid number', 'ERROR']
      },
      {
        ruleId: '5e54dc6500174d3c7cf2bb1a',
        referenceId: '5e54dc6400174d27cc98c236',
        ruleName: '79ff3a29-4439-4844-9e20-aef4d52c9900',
        automatic: false,
        enabled: false,
        salience: null,
        activationGroup: null,
        type: 'FIELD',
        whenCondition: '!isBlank(value)',
        thenCondition: ['The field must be filled', 'ERROR']
      },
      {
        ruleId: '5e54dc6c00174d3c7cf2bb1b',
        referenceId: '5e54dc6b00174d27cc98c237',
        ruleName: 'a0c7f6d5-e958-4582-9ce8-730766ad25ae',
        automatic: false,
        enabled: true,
        salience: null,
        activationGroup: null,
        type: 'FIELD',
        whenCondition: '!isBlank(value)',
        thenCondition: ['The field must be filled', 'ERROR']
      },
      {
        ruleId: '5e54dc6c00174d3c7cf2bb1c',
        referenceId: '5e54dc6b00174d27cc98c237',
        ruleName: 'fe5266d2-6f44-4214-94ee-19f04c53bd65',
        automatic: true,
        enabled: false,
        salience: null,
        activationGroup: null,
        type: 'FIELD',
        whenCondition: '!isDateYYYYMMDD(value)',
        thenCondition: ['The field must be a valid date(YYYYMMDD) ', 'ERROR']
      },
      {
        ruleId: '5e54de7800174d3c7cf2bb1f',
        referenceId: '5e54de7800174d27cc98c239',
        ruleName: '4d905eb9-2f74-4d94-af53-78a4e9cd00fe',
        automatic: true,
        enabled: true,
        salience: null,
        activationGroup: null,
        type: 'FIELD',
        whenCondition: '!isNumber(value)',
        thenCondition: ['The field must be a valid number', 'ERROR']
      }
    ]
  };
};
