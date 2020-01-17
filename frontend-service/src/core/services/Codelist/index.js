import { Add } from './Add';
import { Delete } from './Delete';
import { GetAllInCategory } from './GetAllInCategory';
import { GetById } from './GetById';
import { GetCodelistsList } from './GetCodelistsList';
import { Update } from './Update';

import { codelistRepository } from 'core/domain/model/Codelist/CodelistRepository';

export const CodelistService = {
  addById: Add({ codelistRepository }),
  deleteById: Delete({ codelistRepository }),
  getAllInCategory: GetAllInCategory({ codelistRepository }),
  getById: GetById({ codelistRepository }),
  getCodelistsList: GetCodelistsList({ codelistRepository }),
  updateById: Update({ codelistRepository })
};
