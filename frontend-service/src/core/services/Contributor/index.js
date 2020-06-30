import { Add } from './Add';
import { All } from './All';
import { Delete } from './Delete';
import { UpdateWritePermission } from './UpdateWritePermission';

import { contributorRepository } from 'core/domain/model/Contributor/ContributorRepository';

export const ContributorService = {
  all: All({ contributorRepository }),
  add: Add({ contributorRepository }),
  deleteContributor: Delete({ contributorRepository }),
  updateWritePermission: UpdateWritePermission({ contributorRepository })
};
