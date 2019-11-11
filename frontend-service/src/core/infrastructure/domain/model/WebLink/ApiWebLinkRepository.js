import { apiWebLink } from 'core/infrastructure/api/domain/model/WebLink';
import { WebLink } from 'core/domain/model/WebLink/WebLink';

const all = async dataflowId => {
  const webLinksDTO = await apiWebLink.all(dataflowId);
  return webLinksDTO.map(webLinkDTO => new WebLink(webLinkDTO.id, webLinkDTO.description, webLinkDTO.url));
};

const create = async (dataflowId, weblinkToCreate) => {
  const isCreated = await apiWebLink.create(dataflowId, weblinkToCreate);
  weblinkToCreate.isCreated = isCreated;
  return weblinkToCreate;
};

export const ApiWebLinkRepository = {
  all,
  create
};
