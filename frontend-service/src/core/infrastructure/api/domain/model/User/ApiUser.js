import { UserConfig } from 'conf/domain/model/User';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

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
  userData: async userId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(UserConfig.userData),
      data: { id: userId },
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    // const response = new Promise((resolve, reject) => {
    //   setTimeout(resolve({ hello: 'hola' }), 1000);
    // });
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
