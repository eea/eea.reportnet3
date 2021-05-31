export const routes = {
  ACCESS_POINT: '/',
  CODELISTS: '/codelists',
  DASHBOARDS: '/dataflow/:dataflowId/dashboards',
  DATA_COLLECTION: '/dataflow/:dataflowId/dataCollection/:datasetId',
  DATAFLOW_FEEDBACK_CUSTODIAN: '/dataflow/:dataflowId/feedback',
  DATAFLOW_FEEDBACK: '/dataflow/:dataflowId/feedback/:representativeId',
  DATAFLOW_REPRESENTATIVE: '/dataflow/:dataflowId/provider/:representativeId',
  DATAFLOW: '/dataflow/:dataflowId',
  DATAFLOWS_ERROR: '/dataflows/error/:errorType',
  DATAFLOWS: '/dataflows',
  DATAFLOWS_ID: '/dataflows/:dataflowId',
  DATASET_SCHEMA: '/dataflow/:dataflowId/datasetSchema/:datasetId',
  DATASET: '/dataflow/:dataflowId/dataset/:datasetId',
  DOCUMENTS: '/dataflow/:dataflowId/documents',
  EU_DATASET: '/dataflow/:dataflowId/euDataset/:datasetId',
  EULOGIN: '/eulogin',
  LOGIN: '/login',
  PRIVACY_STATEMENT: '/privacyPolicy',
  PUBLIC_COUNTRIES: '/public/countries',
  PUBLIC_COUNTRY_INFORMATION_ID: '/public/countries/:countryCode',
  PUBLIC_COUNTRY_INFORMATION: '/public/country/:countryCode',
  PUBLIC_DATAFLOW_INFORMATION: '/public/dataflow/:dataflowId',
  PUBLIC_DATAFLOW_INFORMATION_ID: '/public/dataflows/:dataflowId',
  PUBLIC_DATAFLOWS: '/public/dataflows',
  REFERENCE_DATAFLOW: '/referenceDataflow/:referenceDataflowId',
  SETTINGS: '/settings',
  TEST_DATASETS: '/dataflow/:dataflowId/testDatasets'
};
