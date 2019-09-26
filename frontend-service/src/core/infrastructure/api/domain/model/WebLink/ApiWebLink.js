import { config } from 'conf';
import { getUrl } from 'core/infrastructure/api/getUrl';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

export const apiWebLink = {
  all: async dataflowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/list-of-documents.json'
        : getUrl(config.loadDataSetsByDataflowId.url, {
            dataflowId: dataflowId
          }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response.data.weblinks;
  }
};
