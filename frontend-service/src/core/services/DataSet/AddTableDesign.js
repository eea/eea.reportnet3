export const AddTableDesign = ({ datasetRepository }) => async (datasetSchemaId, datasetId, tableSchemaName) =>
  datasetRepository.addTableDesign(datasetSchemaId, datasetId, tableSchemaName);
