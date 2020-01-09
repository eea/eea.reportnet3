import { Add } from './Add';
import { Delete } from './Delete';
import { GetAll } from './GetAll';
import { Update } from './Update';

import { codelistRepository } from 'core/domain/model/Codelist/CodelistRepository';

export const CodelistService = {
  all: GetAll({ codelistRepository }),
  addById: Add({ codelistRepository }),
  deleteById: Delete({ codelistRepository }),
  updateById: Update({ codelistRepository })
};
