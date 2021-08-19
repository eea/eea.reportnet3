import isNil from 'lodash/isNil';

import { config } from 'conf';

const getUserRoleLabel = role => {
  const userRole = Object.values(config.permissions.roles).find(rol => rol.key === role);
  return userRole?.label;
};

const getUserRoles = (userRoles = []) => {
  const userRoleToDataflow = [];
  userRoles.filter(userRol => !userRol.duplicatedRoles && userRoleToDataflow.push(userRol));

  const duplicatedRoles = userRoles.filter(userRol => userRol.duplicatedRoles);
  const dataflowDuplicatedRoles = [];
  for (const duplicatedRol of duplicatedRoles) {
    if (dataflowDuplicatedRoles[duplicatedRol.id]) {
      dataflowDuplicatedRoles[duplicatedRol.id].push(duplicatedRol);
    } else {
      dataflowDuplicatedRoles[duplicatedRol.id] = [duplicatedRol];
    }
  }

  const dataflowPermissionsOrderConfig = {
    0: config.permissions.roles.ADMIN,
    1: config.permissions.roles.CUSTODIAN,
    2: config.permissions.roles.STEWARD,
    3: config.permissions.roles.OBSERVER,
    4: config.permissions.roles.EDITOR_WRITE,
    5: config.permissions.roles.EDITOR_READ,
    6: config.permissions.roles.LEAD_REPORTER,
    7: config.permissions.roles.NATIONAL_COORDINATOR,
    8: config.permissions.roles.REPORTER_WRITE,
    9: config.permissions.roles.REPORTER_READ
  };

  const dataflowPermissions = Object.values(dataflowPermissionsOrderConfig);

  dataflowDuplicatedRoles.forEach(dataflowRoles => {
    let rol = null;

    dataflowPermissions.forEach(permission => {
      dataflowRoles.forEach(dataflowRol => {
        if (isNil(rol) && dataflowRol.userRole === permission.label) {
          rol = dataflowRol;
        }
      });
    });

    userRoleToDataflow.push(rol);
  });

  return userRoleToDataflow;
};

export const UserRoleUtils = {
  getUserRoleLabel,
  getUserRoles
};
