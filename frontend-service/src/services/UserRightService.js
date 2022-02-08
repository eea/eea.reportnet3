import { UserRightRepository } from 'repositories/UserRightRepository';

import { UserRightUtils } from 'services/_utils/UserRightUtils';

export const UserRightService = {
  getReporters: async (dataflowId, dataProviderId) => {
    const userRightListDTO = await UserRightRepository.getReporters(dataflowId, dataProviderId);
    return UserRightUtils.parseUserRightListDTO(userRightListDTO.data);
  },

  getRequesters: async (dataflowId, dataProviderId) => {
    const userRightListDTO = await UserRightRepository.getRequesters(dataflowId, dataProviderId);
    return UserRightUtils.parseUserRightListDTO(userRightListDTO.data);
  },

  deleteReporter: async (userRight, dataflowId, dataProviderId) =>
    await UserRightRepository.deleteReporter(userRight, dataflowId, dataProviderId),

  deleteRequester: async (userRight, dataflowId, dataProviderId) =>
    await UserRightRepository.deleteRequester(userRight, dataflowId, dataProviderId),

  updateReporter: async (userRight, dataflowId, dataProviderId) =>
    await UserRightRepository.updateReporter(userRight, dataflowId, dataProviderId),

  updateRequester: async (userRight, dataflowId) => await UserRightRepository.updateRequester(userRight, dataflowId),

  validateReporters: async (dataflowId, dataProviderId) =>
    await UserRightRepository.validateReporters(dataflowId, dataProviderId)
};
