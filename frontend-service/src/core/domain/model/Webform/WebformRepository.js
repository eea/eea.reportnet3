import { ApiWebformRepository } from 'core/infrastructure/domain/model/Webform/ApiWebformRepository';

export const WebformRepository = {
  addPamsRecords: () => Promise.reject('[WebformRepository#addRecordsById] must be implemented')
};

export const webformRepository = Object.assign({}, WebformRepository, ApiWebformRepository);
