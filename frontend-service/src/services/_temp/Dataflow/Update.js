export const Update = ({ dataflowRepository }) => async (
  dataflowId,
  name,
  description,
  obligationId,
  isReleasable,
  showPublicInfo
) => dataflowRepository.update(dataflowId, name, description, obligationId, isReleasable, showPublicInfo);
