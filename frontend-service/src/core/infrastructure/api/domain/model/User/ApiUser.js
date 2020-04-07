import isEmpty from 'lodash/isEmpty';

import { UserConfig } from 'conf/domain/model/User';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

const parseUserConfiguration = userConfiguration => {
  Object.keys(userConfiguration).forEach(
    key =>
      (userConfiguration[key] = !Array.isArray(userConfiguration[key])
        ? [userConfiguration[key].toString()]
        : !isEmpty(userConfiguration[key])
        ? userConfiguration[key]
        : [])
  );
  return userConfiguration;
};

export const apiUser = {
  login: async code => {
    const tokens = await HTTPRequester.post({
      url: getUrl(UserConfig.login, {
        code
      }),
      queryString: {}
    });
    return tokens.data;
  },

  uploadImg: async (userId, imgData) => {
    const response = await HTTPRequester.postWithFiles({
      url: getUrl(UserConfig.uploadImg, {
        userId
      }),
      queryString: {},
      data: imgData
    });
    return response;
  },
  //implementar token

  // await HTTPRequester.postWithFiles({
  //   url: getUrl(UserConfig.uploadImg, {
  //     userId:
  //   }),
  //   queryString: {},
  //   data: formData,
  //   headers: {
  //     Authorization: `Bearer ${tokens.accessToken}`,
  //     'Content-Type': undefined
  //   }
  // });

  oldLogin: async (userName, password) => {
    const tokens = await HTTPRequester.post({
      url: getUrl(UserConfig.oldLogin, {
        userName,
        password
      }),
      queryString: {}
    });
    return tokens.data;
  },

  configuration: async () => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(UserConfig.configuration),
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      },
      queryString: {}
    });
    return response.data;
  },

  updateAttributes: async userConfiguration => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.update({
      url: getUrl(UserConfig.updateConfiguration),
      queryString: {},
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${tokens.accessToken}`
      },
      data: parseUserConfiguration(userConfiguration)
    });
    return response;
  },
  logout: async refreshToken => {
    const response = await HTTPRequester.post({
      url: getUrl(UserConfig.logout, {
        refreshToken
      }),
      queryString: {}
    });
    return response;
  },

  refreshToken: async refreshToken => {
    const tokens = await HTTPRequester.post({
      url: getUrl(UserConfig.refreshToken, {
        refreshToken
      }),
      queryString: {}
    });
    return tokens.data;
  }
};
