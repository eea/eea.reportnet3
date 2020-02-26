import { ValidationConfig } from 'conf/domain/model/Validation';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

export const apiValidation = {
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
    console.log('DatasetSchemaId: ', datasetSchemaId);
    console.log(
      'url: ',
      getUrl(ValidationConfig.getAll, {
        datasetSchemaId
      })
    );

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
