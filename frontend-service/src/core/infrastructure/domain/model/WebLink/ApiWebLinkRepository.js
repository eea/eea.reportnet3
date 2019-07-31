import { api } from 'core/infrastructure/api';
import { WebLink } from 'core/domain/model/WebLink/WebLink';

const all = async url => {
  const webLinksDTO = await api.webLinks(url);
  return webLinksDTO.map(webLinkDTO => new WebLink(webLinkDTO.description, webLinkDTO.url));
};

export const ApiWebLinkRepository = {
  all
};
