export const DeleteTableDesign = ({ datasetRepository }) => async (datasetId, tableId) =>
  datasetRepository.deleteTableDesign(datasetId, tableId);
