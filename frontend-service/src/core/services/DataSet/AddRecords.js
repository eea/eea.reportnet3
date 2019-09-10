export const AddRecords = ({ dataSetRepository }) => async (dataSetId, tableSchemaId, records) =>
  dataSetRepository.addRecordsById(dataSetId, tableSchemaId, records);
