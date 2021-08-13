import { WebLink } from 'entities/WebLink';

const parseWebLinkListDTO = webLinksDTO => {
  return webLinksDTO?.map(webLinkDTO => new WebLink(webLinkDTO));
};

export const WebLinksUtils = {
  parseWebLinkListDTO
};
