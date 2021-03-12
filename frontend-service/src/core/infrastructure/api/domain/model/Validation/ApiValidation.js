import { ValidationConfig } from 'conf/domain/model/Validation';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiValidation = {
  create: async (datasetSchemaId, validationRule) =>
    await HTTPRequester.update({
      url: getUrl(ValidationConfig.create, { datasetId: datasetSchemaId }),
      data: validationRule
    }),
  deleteById: async (datasetSchemaId, ruleId) =>
    await HTTPRequester.delete({
      url: getUrl(ValidationConfig.delete, {
        datasetSchemaId,
        ruleId
      })
    }),
  getAll: async datasetSchemaId =>
    await HTTPRequester.get({
      url: getUrl(ValidationConfig.getAll, {
        datasetSchemaId
      })
    }),
  update: async (datasetId, validation) => {
    let url = getUrl(ValidationConfig.update, { datasetId });
    if (validation.automatic) {
      url = getUrl(ValidationConfig.updateAutomatic, { datasetId });
    }
    return await HTTPRequester.update({
      url: url,
      data: validation
    });
  }
};
