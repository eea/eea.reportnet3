export const Create = ({ businessDataflowRepository }) => async (
  name,
  description,
  obligationId,
  groupCompaniesId,
  fmeUserId
) => businessDataflowRepository.create(name, description, obligationId, groupCompaniesId, fmeUserId);
