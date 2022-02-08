import { WebLink } from 'entities/WebLink';

const parseWebLinkListDTO = webLinksDTO => webLinksDTO?.map(webLinkDTO => new WebLink(webLinkDTO));

export const WebLinksUtils = {
  parseWebLinkListDTO
};
