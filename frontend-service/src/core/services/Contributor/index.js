import { All } from './All';
import { Delete } from './Delete';
import { Update } from './Update';

import { contributorRepository } from 'core/domain/model/Contributor/ContributorRepository';

export const ContributorService = {
  all: All({ contributorRepository }),
  deleteContributor: Delete({ contributorRepository }),
  update: Update({ contributorRepository })
};
