export const AddTableDesign = ({ datasetRepository }) => async (datasetId, tableSchemaName) =>
  datasetRepository.addTableDesign(datasetId, tableSchemaName);
