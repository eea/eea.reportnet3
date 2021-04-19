import { apiRights } from 'core/infrastructure/api/domain/model/Rights';
import { UserRights } from 'core/domain/model/Rights/UserRights';

import sortBy from 'lodash/sortBy';

const allEditors = async (dataflowId, dataProviderId) => {
  const usersDTO = await apiRights.allEditors(dataflowId, dataProviderId);

  const users = usersDTO.data.map((userDTO, i) => {
    userDTO.id = i + 1;
    return new UserRights(userDTO);
  });

  return sortBy(users, ['account']);
};

const allReporters = async (dataflowId, dataProviderId) => {
  const usersDTO = await apiRights.allReporters(dataflowId, dataProviderId);

  const users = usersDTO.data.map((userDTO, i) => {
    userDTO.id = i + 1;
    return new UserRights(userDTO);
  });

  return sortBy(users, ['account']);
};

const allRequesters = async (dataflowId, dataProviderId) => {
  const usersDTO = await apiRights.allRequesters(dataflowId, dataProviderId);

  const users = usersDTO.data.map((userDTO, i) => {
    userDTO.id = i + 1;
    return new UserRights(userDTO);
  });

  return sortBy(users, ['account']);
};

const deleteEditor = async (account, dataflowId, dataProviderId) => {
  return await apiRights.deleteEditor(account, dataflowId, dataProviderId);
};
const deleteReporter = async (account, dataflowId, dataProviderId) => {
  return await apiRights.deleteReporter(account, dataflowId, dataProviderId);
};
const deleteRequester = async (account, dataflowId, dataProviderId) => {
  return await apiRights.deleteRequester(account, dataflowId, dataProviderId);
};

const updateEditor = async (user, dataflowId, dataProviderId) => {
  return await apiRights.updateEditor(user, dataflowId, dataProviderId);
};
const updateReporter = async (user, dataflowId, dataProviderId) => {
  return await apiRights.updateReporter(user, dataflowId, dataProviderId);
};
const updateRequester = async (user, dataflowId, dataProviderId) => {
  return await apiRights.updateRequester(user, dataflowId, dataProviderId);
};

export const ApiRightsRepository = {
  allEditors,
  allReporters,
  allRequesters,
  deleteEditor,
  deleteReporter,
  deleteRequester,
  updateEditor,
  updateReporter,
  updateRequester
};
