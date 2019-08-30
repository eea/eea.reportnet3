export const AddRecord = ({ dataSetRepository }) => async (dataSetId, tableSchemaId, record) =>
  dataSetRepository.addRecord(dataSetId, tableSchemaId, record);
