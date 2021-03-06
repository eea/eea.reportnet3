import { apiUserRight } from 'core/infrastructure/api/domain/model/UserRight';
import { UserRight } from 'core/domain/model/UserRight/UserRight';

import sortBy from 'lodash/sortBy';
import uniqueId from 'lodash/uniqueId';

const parseUserRightListDTO = userRightListDTO => {
  const userRightList = userRightListDTO.data.map(userRightDTO => {
    userRightDTO.id = uniqueId();
    return new UserRight(userRightDTO);
  });

  return sortBy(userRightList, ['account']);
};

const allReporters = async (dataflowId, dataProviderId) => {
  const userRightListDTO = await apiUserRight.allReporters(dataflowId, dataProviderId);
  return parseUserRightListDTO(userRightListDTO);
};

const allRequesters = async (dataflowId, dataProviderId) => {
  const userRightListDTO = await apiUserRight.allRequesters(dataflowId, dataProviderId);
  return parseUserRightListDTO(userRightListDTO);
};

const deleteReporter = async (userRight, dataflowId, dataProviderId) => {
  return await apiUserRight.deleteReporter(userRight, dataflowId, dataProviderId);
};
const deleteRequester = async (userRight, dataflowId, dataProviderId) => {
  return await apiUserRight.deleteRequester(userRight, dataflowId, dataProviderId);
};

const updateReporter = async (userRight, dataflowId, dataProviderId) => {
  return await apiUserRight.updateReporter(userRight, dataflowId, dataProviderId);
};
const updateRequester = async (userRight, dataflowId) => {
  return await apiUserRight.updateRequester(userRight, dataflowId);
};

export const ApiUserRightRepository = {
  allReporters,
  allRequesters,
  deleteReporter,
  deleteRequester,
  updateReporter,
  updateRequester
};
