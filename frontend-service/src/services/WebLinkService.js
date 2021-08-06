import { WebLinkRepository } from 'repositories/WebLinkRepository';

import { WebLink } from 'entities/WebLink';

export const WebLinkService = {
  getAll: async dataflowId => {
    const response = await WebLinkRepository.getAll(dataflowId);
    return response.data.weblinks.map(webLinkDTO => new WebLink(webLinkDTO));
  },

  create: async (dataflowId, webLinkToCreate) => await WebLinkRepository.create(dataflowId, webLinkToCreate),

  delete: async webLinkId => await WebLinkRepository.delete(webLinkId),

  update: async (dataflowId, webLinkToUpdate) => await WebLinkRepository.update(dataflowId, webLinkToUpdate)
};
