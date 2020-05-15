import { All } from './All';
import { Delete } from './Delete';

import { uniqueConstraintsRepository } from 'core/domain/model/UniqueConstraints/UniqueConstraintsRepository';

export const UniqueConstraintsService = {
  all: All({ uniqueConstraintsRepository }),
  deleteById: Delete({ uniqueConstraintsRepository })
};
