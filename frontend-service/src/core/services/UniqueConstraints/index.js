import { All } from './All';
import { Create } from './Create';
import { Delete } from './Delete';
import { Update } from './Update';

import { uniqueConstraintsRepository } from 'core/domain/model/UniqueConstraints/UniqueConstraintsRepository';

export const UniqueConstraintsService = {
  all: All({ uniqueConstraintsRepository }),
  create: Create({ uniqueConstraintsRepository }),
  deleteById: Delete({ uniqueConstraintsRepository }),
  update: Update({ uniqueConstraintsRepository })
};
