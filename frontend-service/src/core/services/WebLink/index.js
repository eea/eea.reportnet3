import { GetAllWebLinks } from './GetAllWebLinks';
import { webLinkRepository } from 'core/domain/model/WebLink/WebLinkRepository';

export const WebLinkService = {
  all: GetAllWebLinks({ webLinkRepository })
};
