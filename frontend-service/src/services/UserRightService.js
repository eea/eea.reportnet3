import { userRightRepository } from 'repositories/UserRightRepository';
import { UserRight } from 'entities/UserRight';

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
  const userRightListDTO = await userRightRepository.allReporters(dataflowId, dataProviderId);
  return parseUserRightListDTO(userRightListDTO);
};

const allRequesters = async (dataflowId, dataProviderId) => {
  const userRightListDTO = await userRightRepository.allRequesters(dataflowId, dataProviderId);
  return parseUserRightListDTO(userRightListDTO);
};

const deleteReporter = async (userRight, dataflowId, dataProviderId) => {
  return await userRightRepository.deleteReporter(userRight, dataflowId, dataProviderId);
};
const deleteRequester = async (userRight, dataflowId, dataProviderId) => {
  return await userRightRepository.deleteRequester(userRight, dataflowId, dataProviderId);
};

const updateReporter = async (userRight, dataflowId, dataProviderId) => {
  return await userRightRepository.updateReporter(userRight, dataflowId, dataProviderId);
};
const updateRequester = async (userRight, dataflowId) => {
  return await userRightRepository.updateRequester(userRight, dataflowId);
};

export const UserRightService = {
  allReporters,
  allRequesters,
  deleteReporter,
  deleteRequester,
  updateReporter,
  updateRequester
};
