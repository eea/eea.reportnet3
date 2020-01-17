import { CodelistConfig } from 'conf/domain/model/Codelist';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

export const apiCodelist = {
  addById: async codelist => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.post({
      url: getUrl(CodelistConfig.add, {}),
      data: { ...codelist },
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response;
  },
  cloneById: async (codelistId, codelist) => {
    console.log(CodelistConfig.clone);
    const tokens = userStorage.get();
    const response = await HTTPRequester.post({
      url: getUrl(CodelistConfig.clone, { codelistId }),
      data: { ...codelist },
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response;
  },
  all: async () => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(CodelistConfig.all, {}),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response;
  },
  allInCategory: async codelistCategoryId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(CodelistConfig.allInCategory, { codelistCategoryId }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response;
  },
  deleteById: async codelistId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.delete({
      url: getUrl(CodelistConfig.delete, {
        codelistId
      }),
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });

    return response;
  },

  updateById: async codelist => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.update({
      url: getUrl(CodelistConfig.update, {}),
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      },
      data: { ...codelist }
    });
    return response;
  }
};
