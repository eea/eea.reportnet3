import { capitalize, isEmpty, isNull, isUndefined } from 'lodash';

import { apiValidation } from 'core/infrastructure/api/domain/model/Validation';
import { Validation } from 'core/domain/model/Validation/Validation';

const deleteById = async (datasetSchemaId, ruleId) => {
  return await apiValidation.deleteById(datasetSchemaId, ruleId);
};

const getAll = async datasetSchemaId => {
  console.log('Dataset schema Id: ', datasetSchemaId);
  const validationsListDTO = await apiValidation.getAll(datasetSchemaId);

  // const validationsListDTO = {
  //   rulesSchemaId: '5e4a4853a74d0f413db5979e',
  //   idDatasetSchema: '5e4a4771ef3c37e977fe17e3',
  //   rules: [
  //     {
  //       ruleId: '1',
  //       referenceId: '5e4a486519eede2f8821becd',
  //       ruleName: 'FIELD REQUIRED',
  //       shortCode: 'FIELD_REQ',
  //       automatic: true,
  //       enabled: true,
  //       description: 'This value must be filled',
  //       activationGroup: '',
  //       order: 1,
  //       type: 'FIELD',
  //       whenCondition: 'null != null',
  //       thenCondition: ['that field must be filled', 'ERROR']
  //     },
  //     {
  //       ruleId: '25',
  //       referenceId: '7346519eede2f8821becd',
  //       ruleName: 'FIELD REQUIRED',
  //       shortCode: 'FIELD_REQ',
  //       automatic: false,
  //       enabled: true,
  //       description: 'This value must be filled',
  //       activationGroup: '',
  //       order: 2,
  //       type: 'FIELD',
  //       whenCondition: 'null != null',
  //       thenCondition: ['that field must be filled', 'ERROR']
  //     },
  //     {
  //       ruleId: '25',
  //       referenceId: '7346519eede2f8821becd',
  //       ruleName: 'FIELD REQUIRED',
  //       shortCode: 'FIELD_REQ',
  //       automatic: false,
  //       enabled: true,
  //       description: 'This value must be filled',
  //       activationGroup: '',
  //       order: 2,
  //       type: 'FIELD',
  //       whenCondition: 'null != null',
  //       thenCondition: ['that field must be filled', 'ERROR']
  //     },
  //     {
  //       ruleId: '25',
  //       referenceId: '7346519eede2f8821becd',
  //       ruleName: 'FIELD REQUIRED',
  //       shortCode: 'FIELD_REQ',
  //       automatic: false,
  //       enabled: true,
  //       description: 'This value must be filled',
  //       activationGroup: '',
  //       order: 2,
  //       type: 'FIELD',
  //       whenCondition: 'null != null',
  //       thenCondition: ['that field must be filled', 'ERROR']
  //     },
  //     {
  //       ruleId: '25',
  //       referenceId: '7346519eede2f8821becd',
  //       ruleName: 'FIELD REQUIRED',
  //       shortCode: 'FIELD_REQ',
  //       automatic: false,
  //       enabled: true,
  //       description: 'This value must be filled',
  //       activationGroup: '',
  //       order: 2,
  //       type: 'FIELD',
  //       whenCondition: 'null != null',
  //       thenCondition: ['that field must be filled', 'ERROR']
  //     },
  //     {
  //       ruleId: '25',
  //       referenceId: '7346519eede2f8821becd',
  //       ruleName: 'FIELD REQUIRED',
  //       shortCode: 'FIELD_REQ',
  //       automatic: false,
  //       enabled: true,
  //       description: 'This value must be filled',
  //       activationGroup: '',
  //       order: 2,
  //       type: 'FIELD',
  //       whenCondition: 'null != null',
  //       thenCondition: ['that field must be filled', 'ERROR']
  //     },
  //     {
  //       ruleId: '25',
  //       referenceId: '7346519eede2f8821becd',
  //       ruleName: 'FIELD REQUIRED',
  //       shortCode: 'FIELD_REQ',
  //       automatic: false,
  //       enabled: true,
  //       description: 'This value must be filled',
  //       activationGroup: '',
  //       order: 2,
  //       type: 'FIELD',
  //       whenCondition: 'null != null',
  //       thenCondition: ['that field must be filled', 'ERROR']
  //     },
  //     {
  //       ruleId: '25',
  //       referenceId: '7346519eede2f8821becd',
  //       ruleName: 'FIELD REQUIRED',
  //       shortCode: 'FIELD_REQ',
  //       automatic: false,
  //       enabled: true,
  //       description: 'This value must be filled',
  //       activationGroup: '',
  //       order: 2,
  //       type: 'FIELD',
  //       whenCondition: 'null != null',
  //       thenCondition: ['that field must be filled', 'ERROR']
  //     },
  //     {
  //       ruleId: '25',
  //       referenceId: '7346519eede2f8821becd',
  //       ruleName: 'FIELD REQUIRED',
  //       shortCode: 'FIELD_REQ',
  //       automatic: false,
  //       enabled: true,
  //       description: 'This value must be filled',
  //       activationGroup: '',
  //       order: 2,
  //       type: 'FIELD',
  //       whenCondition: 'null != null',
  //       thenCondition: ['that field must be filled', 'ERROR']
  //     },
  //     {
  //       ruleId: '25',
  //       referenceId: '7346519eede2f8821becd',
  //       ruleName: 'FIELD REQUIRED',
  //       shortCode: 'FIELD_REQ',
  //       automatic: false,
  //       enabled: true,
  //       description: 'This value must be filled',
  //       activationGroup: '',
  //       order: 2,
  //       type: 'FIELD',
  //       whenCondition: 'null != null',
  //       thenCondition: ['that field must be filled', 'ERROR']
  //     },
  //     {
  //       ruleId: '25',
  //       referenceId: '7346519eede2f8821becd',
  //       ruleName: 'FIELD REQUIRED',
  //       shortCode: 'FIELD_REQ',
  //       automatic: false,
  //       enabled: true,
  //       description: 'This value must be filled',
  //       activationGroup: '',
  //       order: 2,
  //       type: 'FIELD',
  //       whenCondition: 'null != null',
  //       thenCondition: ['that field must be filled', 'ERROR']
  //     },
  //     {
  //       ruleId: '25',
  //       referenceId: '7346519eede2f8821becd',
  //       ruleName: 'FIELD REQUIRED',
  //       shortCode: 'FIELD_REQ',
  //       automatic: false,
  //       enabled: true,
  //       description: 'This value must be filled',
  //       activationGroup: '',
  //       order: 2,
  //       type: 'FIELD',
  //       whenCondition: 'null != null',
  //       thenCondition: ['that field must be filled', 'ERROR']
  //     },
  //     {
  //       ruleId: '25',
  //       referenceId: '7346519eede2f8821becd',
  //       ruleName: 'FIELD REQUIRED',
  //       shortCode: 'FIELD_REQ',
  //       automatic: false,
  //       enabled: true,
  //       description: 'This value must be filled',
  //       activationGroup: '',
  //       order: 2,
  //       type: 'FIELD',
  //       whenCondition: 'null != null',
  //       thenCondition: ['that field must be filled', 'ERROR']
  //     },
  //     {
  //       ruleId: '25',
  //       referenceId: '7346519eede2f8821becd',
  //       ruleName: 'FIELD REQUIRED',
  //       shortCode: 'FIELD_REQ',
  //       automatic: false,
  //       enabled: true,
  //       description: 'This value must be filled',
  //       activationGroup: '',
  //       order: 2,
  //       type: 'FIELD',
  //       whenCondition: 'null != null',
  //       thenCondition: ['that field must be filled', 'ERROR']
  //     },
  //     {
  //       ruleId: '25',
  //       referenceId: '7346519eede2f8821becd',
  //       ruleName: 'FIELD REQUIRED',
  //       shortCode: 'FIELD_REQ',
  //       automatic: false,
  //       enabled: true,
  //       description: 'This value must be filled',
  //       activationGroup: '',
  //       order: 2,
  //       type: 'FIELD',
  //       whenCondition: 'null != null',
  //       thenCondition: ['that field must be filled', 'ERROR']
  //     },
  //     {
  //       ruleId: '25',
  //       referenceId: '7346519eede2f8821becd',
  //       ruleName: 'FIELD REQUIRED',
  //       shortCode: 'FIELD_REQ',
  //       automatic: false,
  //       enabled: true,
  //       description: 'This value must be filled',
  //       activationGroup: '',
  //       order: 2,
  //       type: 'FIELD',
  //       whenCondition: 'null != null',
  //       thenCondition: ['that field must be filled', 'ERROR']
  //     },
  //     {
  //       ruleId: '25',
  //       referenceId: '7346519eede2f8821becd',
  //       ruleName: 'FIELD REQUIRED',
  //       shortCode: 'FIELD_REQ',
  //       automatic: false,
  //       enabled: true,
  //       description: 'This value must be filled',
  //       activationGroup: '',
  //       order: 2,
  //       type: 'FIELD',
  //       whenCondition: 'null != null',
  //       thenCondition: ['that field must be filled', 'ERROR']
  //     },
  //     {
  //       ruleId: '25',
  //       referenceId: '7346519eede2f8821becd',
  //       ruleName: 'FIELD REQUIRED',
  //       shortCode: 'FIELD_REQ',
  //       automatic: false,
  //       enabled: true,
  //       description: 'This value must be filled',
  //       activationGroup: '',
  //       order: 2,
  //       type: 'FIELD',
  //       whenCondition: 'null != null',
  //       thenCondition: ['that field must be filled', 'ERROR']
  //     },
  //     {
  //       ruleId: '2',
  //       referenceId: '7346519eede2f8821becd',
  //       ruleName: 'FIELD REQUIRED',
  //       shortCode: 'FIELD_REQ',
  //       automatic: 'false',
  //       enabled: 'true',
  //       description: 'This value must be filled',
  //       activationGroup: '',
  //       order: 2,
  //       type: 'TABLE',
  //       whenCondition: 'null != null',
  //       thenCondition: ['that field must be filled', 'ERROR']
  //     }
  //   ]
  // };

  if (isUndefined(validationsListDTO) || isEmpty(validationsListDTO.rules)) {
    return;
  }

  const validationsList = {};
  validationsList.datasetSchemaId = validationsListDTO.idDatasetSchema;
  const rulesData = parseDataValidationRulesDTO(validationsListDTO.rules);
  validationsList.entityTypes = rulesData.entityTypes;
  validationsList.rules = rulesData.rules;
  validationsList.rulesSchemaId = validationsListDTO.rulesSchemaId;
  return validationsList;
};

const parseDataValidationRulesDTO = rulesDTO => {
  const rulesData = {};
  const entityTypes = [];

  rulesData.rules = rulesDTO.map(ruleDTO => {
    entityTypes.push(ruleDTO.type);
    const rule = {};
    rule.shortCode = ruleDTO.shortCode;
    rule.name = ruleDTO.ruleName;
    rule.ruleDescription = ruleDTO.description;
    rule.levelError = ruleDTO.thenCondition[1];
    rule.enabled = capitalize(ruleDTO.enabled.toString());
    rule.automatic = capitalize(ruleDTO.automatic.toString());
    rule.order = ruleDTO.order;
    rule.entityType = ruleDTO.type;
    rule.message = ruleDTO.thenCondition[0];
    return rule;
  });

  rulesData.entityTypes = [...new Set(entityTypes)];
  console.log({ rulesData });
  return rulesData;
};
export const ApiValidationRepository = {
  deleteById,
  getAll
};
