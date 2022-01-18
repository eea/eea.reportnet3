export const WebformConfig = {
  create: '/webform/webformConfig/',
  createPamsRecords: '/dataset/{:datasetId}/insertRecordsMultiTable',
  delete: '/webform/webformConfig/{:id}',
  download: '/webform/webformConfig/{:id}',
  getAll: '/webform/listAll',
  getSinglePamData: '/pam/{:datasetId}/getListSinglePaM/{:groupPaMId}',
  getWebformConfig: '/webform/webformConfig/{:webformId}',
  update: '/webform/webformConfig/'
};
