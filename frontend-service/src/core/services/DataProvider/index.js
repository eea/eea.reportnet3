import { Add } from './Add';
import { Delete } from './Delete';
import { GetAll } from './GetAll';
import { Update } from './Update';

import { dataProviderRepository } from 'core/domain/model/DataProvider/DataProviderRepository';

export const DataProviderService = {
  all: GetAll({ dataProviderRepository }),
  add: Add({ dataProviderRepository }),
  delete: Delete({ dataProviderRepository }),
  update: Update({ dataProviderRepository })
};
