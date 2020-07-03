import { ValidationConfig } from 'conf/domain/model/Validation';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiValidation = {
  create: async (datasetSchemaId, validationRule) => {
    const response = await HTTPRequester.update({
      url: getUrl(ValidationConfig.create, { datasetId: datasetSchemaId }),
      data: validationRule
    });
    return response;
  },
  deleteById: async (datasetSchemaId, ruleId) => {
    const response = await HTTPRequester.delete({
      url: getUrl(ValidationConfig.delete, {
        datasetSchemaId,
        ruleId
      })
    });
    return response;
  },
  getAll: async datasetSchemaId => {
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? `/dataschema/${datasetSchemaId}/fieldSchema`
        : getUrl(ValidationConfig.getAll, {
            datasetSchemaId
          })
    });
    return response.data;
  },
  update: async (datasetId, validation) => {
    let url = getUrl(ValidationConfig.update, { datasetId });
    if (validation.automatic) {
      url = getUrl(ValidationConfig.updateAutomatic, { datasetId });
    }
    const response = await HTTPRequester.update({
      url: url,
      data: validation
    });
    return response;
  }
};
