import { Add } from './Add';
import { Delete } from './Delete';
import { GetAll } from './GetAll';
import { Update } from './Update';

import { codelistCategoryRepository } from 'core/domain/model/CodelistCategory/CodelistCategoryRepository';

export const CodelistCategoryService = {
  all: GetAll({ codelistCategoryRepository }),
  addById: Add({ codelistCategoryRepository }),
  deleteById: Delete({ codelistCategoryRepository }),
  updateById: Update({ codelistCategoryRepository })
};
