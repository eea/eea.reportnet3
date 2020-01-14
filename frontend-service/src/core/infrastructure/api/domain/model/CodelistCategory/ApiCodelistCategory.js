import { CodelistCategoryConfig } from 'conf/domain/model/CodelistCategory';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

export const apiCodelistCategory = {
  all: async () => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(CodelistCategoryConfig.all, {}),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response;
  },
  addById: async category => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.post({
      url: getUrl(CodelistCategoryConfig.add, {}),
      data: { category },
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response;
  },

  deleteById: async codelistCategoryId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.delete({
      url: getUrl(CodelistCategoryConfig.delete, {
        codelistCategoryId
      }),
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });

    return response;
  },

  updateById: async (id, shortCode, description) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.update({
      url: getUrl(CodelistCategoryConfig.update, {}),
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      },
      data: {
        id,
        shortCode,
        description
      }
    });
    return response;
  }
};
