import { Add } from './Add';
import { Delete } from './Delete';
import { GetAll } from './GetAll';
import { GetAllInCategory } from './GetAllInCategory';
import { Update } from './Update';

import { codelistRepository } from 'core/domain/model/Codelist/CodelistRepository';

export const CodelistService = {
  addById: Add({ codelistRepository }),
  all: GetAll({ codelistRepository }),
  deleteById: Delete({ codelistRepository }),
  getAllInCategory: GetAllInCategory({ codelistRepository }),
  updateById: Update({ codelistRepository })
};
