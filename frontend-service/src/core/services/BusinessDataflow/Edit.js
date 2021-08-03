export const Edit = ({ businessDataflowRepository }) => async (
  dataflowId,
  description,
  name,
  groupCompaniesId,
  fmeUserId
) => businessDataflowRepository.edit(dataflowId, description, name, groupCompaniesId, fmeUserId);
