import { apiContributor } from 'core/infrastructure/api/domain/model/Contributor';
import { Contributor } from 'core/domain/model/Contributor/Contributor';

import sortBy from 'lodash/sortBy';

const all = async (dataflowId, dataProviderId) => {
  const contributorsDTO = await apiContributor.all(dataflowId, dataProviderId);

  const contributors = contributorsDTO.data.map((contributorDTO, i) => {
    contributorDTO.id = i + 1;
    return new Contributor(contributorDTO);
  });

  return sortBy(contributors, ['account']);
};

const deleteContributor = async (account, dataflowId, dataProviderId) => {
  return await apiContributor.delete(account, dataflowId, dataProviderId);
};

const update = async (contributor, dataflowId, dataProviderId) => {
  return await apiContributor.update(contributor, dataflowId, dataProviderId);
};

export const ApiContributorRepository = {
  all,
  deleteContributor,
  update
};
