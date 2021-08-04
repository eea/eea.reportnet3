import { ApiWebformRepository } from 'repositories/_temp/model/Webform/ApiWebformRepository';

export const WebformRepository = {
  addPamsRecords: () => Promise.reject('[WebformRepository#addPamsRecords] must be implemented'),
  singlePamData: () => Promise.reject('[WebformRepository#singlePamData] must be implemented')
};

export const webformRepository = Object.assign({}, WebformRepository, ApiWebformRepository);
