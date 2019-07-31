import { api } from 'core/infrastructure/api';
import { WebLink } from 'core/domain/model/WebLink/WebLink';

const all = async () => {
  const webLinksDTO = await api.webLinks();
  return webLinksDTO.map(webLinkDTO => new WebLink(webLinkDTO.description, webLinkDTO.url));
};

export const ApiWebLinkRepository = {
  all
};
