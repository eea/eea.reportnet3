export const ToggleUpdatable = ({ datasetRepository }) => (datasetId, updatable) =>
  datasetRepository.toggleUpdatable(datasetId, updatable);
