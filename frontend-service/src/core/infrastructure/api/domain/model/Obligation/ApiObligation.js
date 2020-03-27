import { ObligationConfig } from 'conf/domain/model/Obligation';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const obligation = {
  openedObligations: async () => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(ObligationConfig.openedObligations),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });

    return response.data;
  }
};
