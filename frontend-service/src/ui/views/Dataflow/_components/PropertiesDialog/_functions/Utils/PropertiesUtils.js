const parseDataToShow = (config, data, dataflowId, messages, user, UserService) => {
  if (UserService && data.obligations) {
    const userRole = UserService.userRole(user, `${config.permissions.DATAFLOW}${dataflowId}`);
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
      },
      dataflowDetails: {
        dataflowStatus: data.status,
        roleDetails: {
          [`${userRole} ${messages['userRoleFunctionality']}`]: data.hasWritePermissions
            ? messages['readWritePermissions']
            : messages['onlyReadPermissions'],
          [`${userRole} ${messages['userRoleType']}`]: '',
          RestApiKey: messages['copyRestAPIKey']
        }
      }
    };
  }
};

export const PropertiesUtils = { parseDataToShow };
