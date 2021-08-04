import { ValidationConfig } from './config/ValidationConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const validationRepository = {
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

  downloadFile: async (datasetId, fileName) =>
    await HTTPRequester.get({
      url: getUrl(ValidationConfig.downloadFile, { datasetId, fileName })
    }),

  generateFile: async datasetId => {
    return await HTTPRequester.post({
      url: getUrl(ValidationConfig.generateFile, { datasetId })
    });
  },

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
