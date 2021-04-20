import { apiUserRight } from 'core/infrastructure/api/domain/model/UserRight';
import { UserRight } from 'core/domain/model/UserRight/UserRight';

import sortBy from 'lodash/sortBy';

const allEditors = async dataflowId => {
  const userRightListDTO = await apiUserRight.allEditors(dataflowId);

  const userRightList = userRightListDTO.data.map((userRightDTO, i) => {
    userRightDTO.id = i + 1;
    return new UserRight(userRightDTO);
  });

  return sortBy(userRightList, ['account']);
};

const allReporters = async dataflowId => {
  const userRightListDTO = await apiUserRight.allReporters(dataflowId);

  const userRightList = userRightListDTO.data.map((userRightDTO, i) => {
    userRightDTO.id = i + 1;
    return new UserRight(userRightDTO);
  });

  return sortBy(userRightList, ['account']);
};

const allRequesters = async (dataflowId, dataProviderId) => {
  const userRightListDTO = await apiUserRight.allRequesters(dataflowId, dataProviderId);

  const userRightList = userRightListDTO.data.map((userRightDTO, i) => {
    userRightDTO.id = i + 1;
    return new UserRight(userRightDTO);
  });

  return sortBy(userRightList, ['account']);
};

const deleteEditor = async (account, dataflowId, dataProviderId) => {
  return await apiUserRight.deleteEditor(account, dataflowId, dataProviderId);
};
const deleteReporter = async (account, dataflowId, dataProviderId) => {
  return await apiUserRight.deleteReporter(account, dataflowId, dataProviderId);
};
const deleteRequester = async (account, dataflowId, dataProviderId) => {
  return await apiUserRight.deleteRequester(account, dataflowId, dataProviderId);
};

const updateEditor = async (userRight, dataflowId, dataProviderId) => {
  return await apiUserRight.updateEditor(userRight, dataflowId, dataProviderId);
};
const updateReporter = async (userRight, dataflowId, dataProviderId) => {
  return await apiUserRight.updateReporter(userRight, dataflowId, dataProviderId);
};
const updateRequester = async (userRight, dataflowId, dataProviderId) => {
  return await apiUserRight.updateRequester(userRight, dataflowId, dataProviderId);
};

export const ApiUserRightRepository = {
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
