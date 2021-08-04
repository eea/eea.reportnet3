import { ApiWebLinkRepository } from 'repositories/_temp/model/WebLink/ApiWebLinkRepository';

export const WebLinkRepository = {
  all: () => Promise.reject('[WebLinkRepository#all] must be implemented'),
  create: () => Promise.reject('[WebLinkRepository#create] must be implemented'),
  deleteWebLink: () => Promise.reject('[WebLinkRepository#deleteWebLink] must be implemented'),
  update: () => Promise.reject('[WebLinkRepository#update] must be implemented')
};

export const webLinkRepository = Object.assign({}, WebLinkRepository, ApiWebLinkRepository);
