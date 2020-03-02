import { ValidationConfig } from 'conf/domain/model/Validation';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

export const apiValidation = {
  create: async (datasetSchemaId, validationRule) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.update({
      url: getUrl(ValidationConfig.create, { datasetSchemaId }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      },
      data: validationRule
    });
    return response;
  },
  deleteById: async (datasetSchemaId, ruleId) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.delete({
      url: getUrl(ValidationConfig.delete, {
        datasetSchemaId,
        ruleId
      }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response;
  },
  getAll: async datasetSchemaId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? `/dataschema/${datasetSchemaId}/fieldSchema`
        : getUrl(ValidationConfig.getAll, {
            datasetSchemaId
          }),
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data;
  }
};
