import { UserRightRepository } from 'repositories/UserRightRepository';
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

export const UserRightService = {
  getReporters: async (dataflowId, dataProviderId) => {
    const userRightListDTO = await UserRightRepository.getReporters(dataflowId, dataProviderId);
    return parseUserRightListDTO(userRightListDTO);
  },

  getRequesters: async (dataflowId, dataProviderId) => {
    const userRightListDTO = await UserRightRepository.getRequesters(dataflowId, dataProviderId);
    return parseUserRightListDTO(userRightListDTO);
  },

  deleteReporter: async (userRight, dataflowId, dataProviderId) => {
    return await UserRightRepository.deleteReporter(userRight, dataflowId, dataProviderId);
  },
  deleteRequester: async (userRight, dataflowId, dataProviderId) => {
    return await UserRightRepository.deleteRequester(userRight, dataflowId, dataProviderId);
  },
  updateReporter: async (userRight, dataflowId, dataProviderId) => {
    return await UserRightRepository.updateReporter(userRight, dataflowId, dataProviderId);
  },
  updateRequester: async (userRight, dataflowId) => {
    return await UserRightRepository.updateRequester(userRight, dataflowId);
  }
};
