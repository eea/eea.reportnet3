import { config } from 'conf';
import { getUrl } from 'core/infrastructure/api/getUrl';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiUser = {
  login: async (userName, password) => {
    const tokens = await HTTPRequester.post({
      url: window.env.REACT_APP_JSON
        ? ''
        : getUrl(config.loginUser.url, {
            userName,
            password
          }),
      queryString: {}
    });
    return tokens.data;
  },
  logout: async userId => {},
  refreshToken: async refreshToken => {
    const tokens = await HTTPRequester.post({
      url: window.env.REACT_APP_JSON
        ? ''
        : getUrl(config.refreshToken.url, {
            refreshToken
          }),
      queryString: {}
    });
    return tokens.data;
  }
};
