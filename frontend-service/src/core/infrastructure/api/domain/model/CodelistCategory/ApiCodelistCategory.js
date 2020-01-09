import { CodelistCategoryConfig } from 'conf/domain/model/CodelistCategory';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

export const apiCodelistCategory = {
  all: async dataflowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(CodelistCategoryConfig.all, {
        dataflowId
      }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response;
  },
  addById: async (dataflowId, name, description, codelists) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.post({
      url: getUrl(CodelistCategoryConfig.add, {
        dataflowId
      }),
      data: {
        description,
        name,
        codelists
      },
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response;
  },

  deleteById: async (dataflowId, categoryId) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.delete({
      url: getUrl(CodelistCategoryConfig.delete, {
        dataflowId,
        categoryId
      }),
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });

    return response;
  },

  updateById: async (dataflowId, categoryId, category) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.update({
      url: getUrl(CodelistCategoryConfig.update, {
        dataflowId,
        categoryId
      }),
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      },
      data: {
        category
      }
    });
    return response;
  }
};
