import { webformRepository } from 'core/domain/model/Webform/WebformRepository';

import { AddPamsRecords } from './AddPamsRecords';
import { GetSingleData } from './GetSingleData';

export const WebformService = {
  addPamsRecords: AddPamsRecords({ webformRepository }),
  singlePamData: GetSingleData({ webformRepository })
};
