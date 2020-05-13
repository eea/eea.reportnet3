import { All } from './All';

import { uniqueConstraintsRepository } from 'core/domain/model/UniqueConstraints/UniqueConstraintsRepository';

export const UniqueConstraintsService = { all: All({ uniqueConstraintsRepository }) };
