import { apiWebLink } from 'core/infrastructure/api/domain/model/WebLink';
import { WebLink } from 'core/domain/model/WebLink/WebLink';

const all = async dataFlowId => {
  const webLinksDTO = await apiWebLink.all(dataFlowId);
  return webLinksDTO.map(webLinkDTO => new WebLink(webLinkDTO.description, webLinkDTO.url));
};

export const ApiWebLinkRepository = {
  all
};
