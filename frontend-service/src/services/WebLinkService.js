import { webLinkRepository } from 'repositories/WebLinkRepository';

import { WebLink } from 'entities/WebLink';

const all = async dataflowId => {
  const webLinksDTO = await webLinkRepository.all(dataflowId);
  webLinksDTO.data.weblinks = webLinksDTO.data.weblinks.map(webLinkDTO => new WebLink(webLinkDTO));
  return webLinksDTO;
};

const create = async (dataflowId, weblinkToCreate) => await webLinkRepository.create(dataflowId, weblinkToCreate);

const deleteWebLink = async weblinkToDelete => await webLinkRepository.deleteWebLink(weblinkToDelete);

const update = async (dataflowId, weblinkToUpdate) => await webLinkRepository.update(dataflowId, weblinkToUpdate);

export const WebLinkService = {
  all,
  create,
  deleteWebLink,
  update
};
