import { api } from 'core/infrastructure/api';
import { WebLink } from 'core/domain/model/WebLink/WebLink';

const all = async dataFlowId => {
  const webLinksDTO = await api.webLinks(dataFlowId);
  return webLinksDTO.map(webLinkDTO => new WebLink(webLinkDTO.description, webLinkDTO.url));
};

export const ApiWebLinkRepository = {
  all
};
