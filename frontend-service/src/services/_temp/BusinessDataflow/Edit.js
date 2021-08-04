export const Edit = ({ businessDataflowRepository }) => async (
  dataflowId,
  description,
  obligationId,
  name,
  groupCompaniesId,
  fmeUserId
) => businessDataflowRepository.edit(dataflowId, description, obligationId, name, groupCompaniesId, fmeUserId);
