const parseDataflowData = (config, data, dataflowId, messages, user, UserService) => {
  if (UserService) {
    const userRole = UserService.userRole(user, `${config.permissions.DATAFLOW}${dataflowId}`);

    return {
      dataflowStatus: data.status,
      roleDetails: {
        [`${userRole} ${messages['userRoleFunctionality']}`]: data.hasWritePermissions
          ? messages['readWritePermissions']
          : messages['onlyReadPermissions'],
        [`${userRole} ${messages['userRoleType']}`]: '',
        [`${messages['restApiKey']}`]: messages['copyRestAPIKey']
      }
    };
  }
};

const parseObligationsData = data => {
  if (data.obligations) {
    return {
      obligation: {
        title: data.obligations.title,
        description: data.obligations.description,
        comment: data.obligations.comment,
        expirationDate: data.obligations.expirationDate
      },
      legalInstrument: {
        alias: data.obligations.legalInstruments.alias,
        title: data.obligations.legalInstruments.title
      }
    };
  }
};

export const PropertiesUtils = { parseDataflowData, parseObligationsData };
