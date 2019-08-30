import { GetAll } from './GetAll';
import { webLinkRepository } from 'core/domain/model/WebLink/WebLinkRepository';

export const WebLinkService = {
  all: GetAll({ webLinkRepository })
};
