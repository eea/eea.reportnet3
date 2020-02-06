import { UserConfig } from 'conf/domain/model/User';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

export const apiUser = {
  login: async code => {
    const tokens = await HTTPRequester.post({
      url: window.env.REACT_APP_JSON
        ? ''
        : getUrl(UserConfig.login, {
            code
          }),
      queryString: {}
    });
    return tokens.data;
  },
  oldLogin: async (userName, password) => {
    const tokens = await HTTPRequester.post({
      url: window.env.REACT_APP_JSON
        ? ''
        : getUrl(UserConfig.oldLogin, {
            userName,
            password
          }),
      queryString: {}
    });
    return tokens.data;
  },
  logout: async refreshToken => {
    const response = await HTTPRequester.post({
      url: window.env.REACT_APP_JSON
        ? ''
        : getUrl(UserConfig.logout, {
            refreshToken
          }),
      queryString: {}
    });
    return response;
  },
  refreshToken: async refreshToken => {
    const tokens = await HTTPRequester.post({
      url: window.env.REACT_APP_JSON
        ? ''
        : getUrl(UserConfig.refreshToken, {
            refreshToken
          }),
      queryString: {}
    });
    return tokens.data;
  }
};
