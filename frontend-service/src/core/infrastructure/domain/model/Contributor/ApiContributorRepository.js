import { apiContributor } from 'core/infrastructure/api/domain/model/Contributor';
import { Contributor } from 'core/domain/model/Contributor/Contributor';
import isEmpty from 'lodash/isEmpty';

const allEditors = async dataflowId => {
  const contributorsDTO = await apiContributor.allEditors(dataflowId);

  const contributors = !isEmpty(contributorsDTO)
    ? contributorsDTO.map(contributorDTO => new Contributor(contributorDTO))
    : [];

  console.log('editors', contributors);
  return contributors;
};

const allReporters = async (dataflowId, dataProviderId) => {
  const contributorsDTO = await apiContributor.allReporters(dataflowId, dataProviderId);

  const contributors = !isEmpty(contributorsDTO)
    ? contributorsDTO.map(contributorDTO => new Contributor(contributorDTO))
    : [];

  console.log('reporters', contributors);
  return contributors;
};
const deleteEditor = async (editorAccount, dataflowId) => {
  return await apiContributor.deleteEditor(editorAccount, dataflowId);
};

const deleteReporter = async (reporterAccount, dataflowId, dataProviderId) => {
  return await apiContributor.deleteReporter(reporterAccount, dataflowId, dataProviderId);
};

const updateEditor = async (contributor, dataflowId) => {
  console.log('editor', contributor);
  return await apiContributor.updateEditor(contributor, dataflowId);
};

const updateReporter = async (contributor, dataflowId, dataProviderId) => {
  console.log('reporter', contributor);
  return await apiContributor.updateReporter(contributor, dataflowId, dataProviderId);
};

export const ApiContributorRepository = {
  allEditors,
  allReporters,
  deleteEditor,
  deleteReporter,
  updateEditor,
  updateReporter
};
