import { apiWebLink } from 'core/infrastructure/api/domain/model/WebLink';
import { WebLink } from 'core/domain/model/WebLink/WebLink';

const all = async dataflowId => {
  const webLinksDTO = await apiWebLink.all(dataflowId);
  return webLinksDTO.map(webLinkDTO => new WebLink(webLinkDTO.id, webLinkDTO.description, webLinkDTO.url));
};

export const ApiWebLinkRepository = {
  all
};
