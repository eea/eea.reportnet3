export const ToggleUpdatable = ({ datasetRepository }) => async (datasetId, updatable) =>
  datasetRepository.toggleUpdatable(datasetId, updatable);
