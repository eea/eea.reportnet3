import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { UniqueConstraintsConfig } from 'conf/domain/model/UniqueConstraints';
import { userStorage } from 'core/domain/model/User/UserStorage';

export const apiUniqueConstraints = {
  all: async datasetSchemaId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(UniqueConstraintsConfig.all, { datasetSchemaId }),
      queryString: {},
      headers: { Authorization: `Bearer ${tokens.accessToken}` }
    });
    return response.data;
  }
};
