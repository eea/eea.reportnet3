export const AddRecords = ({ datasetRepository }) => async (datasetId, tableSchemaId, records) =>
  datasetRepository.addRecordsById(datasetId, tableSchemaId, records);
