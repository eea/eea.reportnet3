import { GetAll } from './GetAll';
import { Create } from './Create';
import { Delete } from './Delete';
import { Update } from './Update';

import { webLinkRepository } from 'core/domain/model/WebLink/WebLinkRepository';

export const WebLinkService = {
  all: GetAll({ webLinkRepository }),
  create: Create({ webLinkRepository }),
  deleteWebLink: Delete({ webLinkRepository }),
  update: Update({ webLinkRepository })
};
