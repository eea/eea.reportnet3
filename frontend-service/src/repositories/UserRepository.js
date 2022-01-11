import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

import { UserConfig } from './config/UserConfig';
import { getUrl } from './_utils/UrlUtils';
import { HTTPRequester } from './_utils/HTTPRequester';

export const UserRepository = {
  getConfiguration: async () => {
    const response = await HTTPRequester.get({ url: getUrl(UserConfig.getConfiguration) });
    return parseUserImage(response.data);
  },

  getUserInfo: async userId => await HTTPRequester.get({ url: getUrl(UserConfig.getUserInfo, { userId }) }),

  login: async code => await HTTPRequester.post({ url: getUrl(UserConfig.login, { code }) }),

  oldLogin: async (userName, password) =>
    await HTTPRequester.post({ url: getUrl(UserConfig.oldLogin, { userName, password }) }),

  updateConfiguration: async userConfiguration =>
    await HTTPRequester.update({
      url: getUrl(UserConfig.updateConfiguration),
      headers: { 'Content-Type': 'application/json' },
      data: parseUserConfiguration(userConfiguration)
    }),

  logout: async refreshToken => await HTTPRequester.post({ url: getUrl(UserConfig.logout, { refreshToken }) }),

  refreshToken: async refreshToken =>
    await HTTPRequester.post({ url: getUrl(UserConfig.refreshToken, { refreshToken }) })
};

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
    if (!isNil(data.userImage)) {
      const undefinedUserImage = data.userImage.filter(
        token => token.split('~')[1] === 'undefined' || token.split('~')[1] === undefined
      );
      if (!isUndefined(undefinedUserImage) && undefinedUserImage.length > 0) {
        data.userImage = [];
      } else {
        data.userImage.sort();
        data.userImage = data.userImage.map(token => token.split('~')[1]);
        return data;
      }
    } else {
      return data;
    }
  }
};
