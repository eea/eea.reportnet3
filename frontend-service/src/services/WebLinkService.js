import { WebLinkRepository } from 'repositories/WebLinkRepository';

import { WebLinksUtils } from 'services/_utils/WebLinksUtils';

export const WebLinkService = {
  getAll: async dataflowId => {
    const response = await WebLinkRepository.getAll(dataflowId);
    return WebLinksUtils.parseWebLinkListDTO(response.data);
  },

  create: async (dataflowId, webLinkToCreate) => await WebLinkRepository.create(dataflowId, webLinkToCreate),

  delete: async webLinkId => await WebLinkRepository.delete(webLinkId),

  update: async (dataflowId, webLinkToUpdate) => await WebLinkRepository.update(dataflowId, webLinkToUpdate)
};
