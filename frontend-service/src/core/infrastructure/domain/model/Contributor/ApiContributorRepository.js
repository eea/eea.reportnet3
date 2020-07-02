import { apiContributor } from 'core/infrastructure/api/domain/model/Contributor';
import { Contributor } from 'core/domain/model/Contributor/Contributor';
import isEmpty from 'lodash/isEmpty';

const all = async (dataflowId, dataProviderId) => {
  const contributorsDTO = await apiContributor.all(dataflowId, dataProviderId);

  const contributors = contributorsDTO.map(contributorDTO => new Contributor(contributorDTO));
  return contributors;
};

const deleteContributor = async (editorAccount, dataflowId, dataProviderId) => {
  return await apiContributor.delete(editorAccount, dataflowId, dataProviderId);
};

const update = async (contributor, dataflowId, dataProviderId) => {
  return await apiContributor.update(contributor, dataflowId, dataProviderId);
};

export const ApiContributorRepository = {
  all,
  deleteContributor,
  update
};
