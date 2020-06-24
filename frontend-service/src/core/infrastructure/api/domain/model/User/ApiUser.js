import isEmpty from 'lodash/isEmpty';
import isUndefined from 'lodash/isUndefined';

import { UserConfig } from 'conf/domain/model/User';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';

const parseUserConfiguration = userConfiguration => {
  userConfiguration.userImage = userConfiguration.userImage.map((token, i) => `${('000' + i).substr(-3)}~${token}`);
  Object.keys(userConfiguration).forEach(
    key =>
      (userConfiguration[key] = !isUndefined(userConfiguration[key])
        ? !Array.isArray(userConfiguration[key])
          ? [userConfiguration[key].toString()]
          : !isEmpty(userConfiguration[key])
          ? userConfiguration[key]
          : []
        : [])
  );
  return userConfiguration;
};

const parseUserImage = data => {
  if (!isUndefined(data) && !isEmpty(data)) {
    data.userImage.sort();
    data.userImage = data.userImage.map(token => token.split('~')[1]);
    return data;
  }
};

export const apiUser = {
  login: async code => {
    const tokens = await HTTPRequester.post({
      url: getUrl(UserConfig.login, {
        code
      })
    });
    return tokens.data;
  },

  uploadImg: async (userId, imgData) => {
    const response = await HTTPRequester.postWithFiles({
      url: getUrl(UserConfig.uploadImg, {
        userId
      }),
      data: imgData
    });
    return response;
  },
  oldLogin: async (userName, password) => {
    const tokens = await HTTPRequester.post({
      url: getUrl(UserConfig.oldLogin, {
        userName,
        password
      })
    });
    return tokens.data;
  },

  configuration: async () => {
    const response = await HTTPRequester.get({
      url: getUrl(UserConfig.configuration)
    });
    return parseUserImage(response.data);
  },

  updateAttributes: async userConfiguration => {
    const response = await HTTPRequester.update({
      url: getUrl(UserConfig.updateConfiguration),
      headers: {
        'Content-Type': 'application/json'
      },
      data: parseUserConfiguration(userConfiguration)
    });
    return response;
  },
  logout: async refreshToken => {
    const response = await HTTPRequester.post({
      url: getUrl(UserConfig.logout, {
        refreshToken
      })
    });
    return response;
  },

  refreshToken: async refreshToken => {
    const tokens = await HTTPRequester.post({
      url: getUrl(UserConfig.refreshToken, {
        refreshToken
      })
    });
    return tokens.data;
  }
};
