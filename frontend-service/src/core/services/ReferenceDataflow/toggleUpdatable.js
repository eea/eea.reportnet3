export const ToggleUpdatable = ({ referenceDataflowRepository }) => (referenceDataflowId, updatable) =>
  referenceDataflowRepository.toggleUpdatable(referenceDataflowId, updatable);
