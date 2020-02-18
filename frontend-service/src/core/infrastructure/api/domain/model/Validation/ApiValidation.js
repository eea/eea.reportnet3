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
  }
};
