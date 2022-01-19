import isEmpty from 'lodash/isEmpty';

import { config } from 'conf';

import { TextUtils } from 'repositories/_utils/TextUtils';

const getUserRoleLabel = role => {
  const userRole = Object.values(config.permissions.roles).find(rol => rol.key === role);
  return userRole?.label;
};

const getUserRoleByDataflow = (dataflowId, accessRoles = [], contextRoles = []) => {
  if (accessRoles.some(role => role === config.permissions.roles.ADMIN.key)) {
    return config.permissions.roles.ADMIN.label;
  }

  const dataflowRoles = contextRoles
    .filter(role => role.includes(`${config.permissions.prefixes.DATAFLOW}${dataflowId}-`))
    .map(role => TextUtils.reduceString(role, `${role.replace(/\D/g, '')}-`));

  if (isEmpty(dataflowRoles)) {
    return null;
  }

  const dataflowPermissions = [
    config.permissions.roles.CUSTODIAN,
    config.permissions.roles.STEWARD,
    config.permissions.roles.STEWARD_SUPPORT,
    config.permissions.roles.OBSERVER,
    config.permissions.roles.EDITOR_WRITE,
    config.permissions.roles.EDITOR_READ,
    config.permissions.roles.LEAD_REPORTER,
    config.permissions.roles.NATIONAL_COORDINATOR,
    config.permissions.roles.REPORTER_WRITE,
    config.permissions.roles.REPORTER_READ
  ];

  const permissions = dataflowPermissions.filter(permission => dataflowRoles.includes(permission.key));

  if (isEmpty(permissions) || permissions.length === 0) {
    return '';
  }

  return UserRoleUtils.getUserRoleLabel(permissions[0].key);
};

export const UserRoleUtils = {
  getUserRoleLabel,
  getUserRoleByDataflow
};
