export const DeleteTableDesign = ({ datasetRepository }) => async (datasetSchemaId, tableId) =>
  datasetRepository.deleteTableDesign(datasetSchemaId, tableId);
