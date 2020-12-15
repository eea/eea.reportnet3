import { webformRepository } from 'core/domain/model/Webform/WebformRepository';

import { AddPamsRecords } from './AddPamsRecords';

export const WebformService = { addPamsRecords: AddPamsRecords({ webformRepository }) };
