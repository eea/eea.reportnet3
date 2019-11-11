import { ApiWebLinkRepository } from 'core/infrastructure/domain/model/WebLink/ApiWebLinkRepository';

export const WebLinkRepository = {
  all: () => Promise.reject('[WebLinkRepository#all] must be implemented'),
  create: () => Promise.reject('[WebLinkRepository#all] must be implemented')
};

export const webLinkRepository = Object.assign({}, WebLinkRepository, ApiWebLinkRepository);
