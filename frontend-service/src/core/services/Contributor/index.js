import { Add } from './Add';
import { Delete } from './Delete';
import { GetAll } from './GetAll';
import { Update } from './Update';

import { contributorRepository } from 'core/domain/model/Contributor/ContributorRepository';

export const ContributorService = {
  all: GetAll({ contributorRepository }),
  addByLogin: Add({ contributorRepository }),
  deleteById: Delete({ contributorRepository }),
  updateById: Update({ contributorRepository })
};
