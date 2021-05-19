import { apiUserRight } from 'core/infrastructure/api/domain/model/UserRight';
import { UserRight } from 'core/domain/model/UserRight/UserRight';

import sortBy from 'lodash/sortBy';
import uniqueId from 'lodash/uniqueId';

import { config } from 'conf';

const getUserRoleLabel = role => {
  const userRole = Object.values(config.permissions.roles).find(rol => rol.key === role);
  return userRole?.label;
};

const getUserNameKey = label => {
  const userRole = Object.values(config.permissions.roles).find(rol => rol.label === label);
  return userRole?.key;
};

const parseUserRightListDTO = userRightListDTO => {
  const userRightList = userRightListDTO.data.map(userRightDTO => {
    userRightDTO.id = uniqueId();
    return new UserRight({
      account: userRightDTO.account,
      id: userRightDTO.id,
      role: getUserRoleLabel(userRightDTO.role)
    });
  });

  return sortBy(userRightList, ['account']);
};

const parseUserRight = userRight => {
  return new UserRight({
    account: userRight.account,
    id: userRight.id,
    isNew: userRight.isNew,
    role: getUserNameKey(userRight.role)
  });
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
  return await apiUserRight.deleteReporter(parseUserRight(userRight), dataflowId, dataProviderId);
};
const deleteRequester = async (userRight, dataflowId, dataProviderId) => {
  return await apiUserRight.deleteRequester(parseUserRight(userRight), dataflowId, dataProviderId);
};

const updateReporter = async (userRight, dataflowId, dataProviderId) => {
  return await apiUserRight.updateReporter(parseUserRight(userRight), dataflowId, dataProviderId);
};
const updateRequester = async (userRight, dataflowId) => {
  return await apiUserRight.updateRequester(parseUserRight(userRight), dataflowId);
};

export const ApiUserRightRepository = {
  allReporters,
  allRequesters,
  deleteReporter,
  deleteRequester,
  updateReporter,
  updateRequester
};
