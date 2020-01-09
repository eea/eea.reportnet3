import { CodelistConfig } from 'conf/domain/model/Codelist';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { userStorage } from 'core/domain/model/User/UserStorage';

export const apiCodelist = {
  all: async dataflowId => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.get({
      url: getUrl(CodelistConfig.all, {
        dataflowId
      }),
      queryString: {},
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response;
  },
  addById: async (dataflowId, description, items, name, status, version) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.post({
      url: getUrl(CodelistConfig.add, {
        dataflowId
      }),
      data: {
        description,
        items,
        name,
        status,
        version
      },
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });
    return response;
  },

  deleteById: async (dataflowId, codelistId) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.delete({
      url: getUrl(CodelistConfig.delete, {
        dataflowId,
        codelistId
      }),
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      }
    });

    return response;
  },

  updateById: async (dataflowId, codelistId, codelist) => {
    const tokens = userStorage.get();
    const response = await HTTPRequester.update({
      url: getUrl(CodelistConfig.update, {
        dataflowId
      }),
      headers: {
        Authorization: `Bearer ${tokens.accessToken}`
      },
      data: {
        codelistId,
        codelist
      }
    });
    return response;
  }
};
