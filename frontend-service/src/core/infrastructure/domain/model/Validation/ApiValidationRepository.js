import { isEmpty, isNull, isUndefined } from 'lodash';

import { apiValidation } from 'core/infrastructure/api/domain/model/Validation';
import { Validation } from 'core/domain/model/Validation/Validation';

const deleteById = async (datasetSchemaId, ruleId) => {
  return await apiValidation.deleteById(datasetSchemaId, ruleId);
};

const getAll = async datasetSchemaId => {
  // const validationsDTO = await apiValidation.getAll(datasetSchemaId);

  const validationsListDTO = {
    rulesSchemaId: '5e4a4853a74d0f413db5979e',
    idDatasetSchema: '5e4a4771ef3c37e977fe17e3',
    rules: [
      {
        ruleId: '1',
        referenceId: '5e4a486519eede2f8821becd',
        ruleName: 'Date between 2005 and 2010',
        automatic: true,
        enabled: true,
        salience: 1,
        activationGroup: '',
        order: 1,
        type: 'FIELD',
        whenCondition: 'null != null',
        thenCondition: ['that field must be filled', 'ERROR']
      },
      {
        ruleId: '2',
        referenceId: '7346519eede2f8821becd',
        ruleName: 'Date between 2010 and 2020',
        automatic: false,
        enabled: true,
        salience: 2,
        activationGroup: '',
        order: 2,
        type: 'FIELD',
        whenCondition: 'null != null',
        thenCondition: ['that field must be filled', 'ERROR']
      },
      {
        ruleId: '2',
        referenceId: '7346519eede2f8821becd',
        ruleName: 'Date between 2010 and 2020',
        automatic: false,
        enabled: true,
        salience: 2,
        activationGroup: '',
        order: 2,
        type: 'TABLE',
        whenCondition: 'null != null',
        thenCondition: ['that field must be filled', 'ERROR']
      }
    ]
  };

  if (isUndefined(validationsListDTO) || isEmpty(validationsListDTO.rules)) {
    return;
  }

  const validationsList = {};
  validationsList.rulesSchemaId = validationsListDTO.rulesSchemaId;
  validationsList.datasetSchemaId = validationsListDTO.idDatasetSchema;
  validationsList.rules = parseDataValidationRulesDTO(validationsListDTO.rules);
  console.log({ validationsList });
  return validationsList;
};

const parseDataValidationRulesDTO = rulesDTO => {
  const rulesData = {};
  const entityLevels = [];

  rulesData.rules = rulesDTO.map(ruleDTO => {
    entityLevels.push(ruleDTO.type);
    return new Validation({
      id: ruleDTO.ruleId,
      levelError: ruleDTO.salience,
      entityType: ruleDTO.type,
      message: ruleDTO.thenCondition[0]
    });
  });

  rulesData.entityLevels = [...new Set(entityLevels)];

  return rulesData;
};
export const ApiValidationRepository = {
  deleteById,
  getAll
};
