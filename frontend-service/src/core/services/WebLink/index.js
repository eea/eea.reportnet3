import { GetAll } from './GetAll';
import { Create } from './Create';
import { webLinkRepository } from 'core/domain/model/WebLink/WebLinkRepository';

export const WebLinkService = {
  create: Create({ webLinkRepository }),
  all: GetAll({ webLinkRepository })
};
