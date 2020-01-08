import { Create } from './Create';
import { dataCollectionRepository } from 'core/domain/model/DataCollection/DataCollectionRepository';

export const DataCollectionService = {
  create: Create({ dataCollectionRepository })
};
