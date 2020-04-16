import isNil from 'lodash/isNil';
import moment from 'moment';

const camelCaseToNormal = str => str.replace(/([A-Z])/g, ' $1').replace(/^./, str2 => str2.toUpperCase());

const parseDataflowData = (config, data, dataflowId, messages, user, UserService) => {
  if (UserService) {
    const userRole = UserService.userRole(user, `${config.permissions.DATAFLOW}${dataflowId}`);

    return {
      dataflowStatus: data.status,
      roleDetails: {
        [`${userRole} ${messages['userRoleFunctionality']}`]: data.hasWritePermissions
          ? messages['readWritePermissions']
          : messages['onlyReadPermissions']
        // [`${userRole} ${messages['userRoleType']}`]: '',
        // [`${messages['restApiKey']}`]: messages['copyRestAPIKey']
      }
    };
  }
};

const parseObligationsData = (data, format) => {
  if (data.obligations) {
    return [
      {
        label: 'obligation',
        data: {
          title: data.obligations.title,
          description: data.obligations.description,
          comment: data.obligations.comment,
          expirationDate: !isNil(data.obligations.expirationDate)
            ? moment(data.obligations.expirationDate).format(format)
            : '-'
        }
      },
      {
        label: 'legalInstrument',
        data: {
          alias: data.obligations.legalInstruments.alias,
          title: data.obligations.legalInstruments.title
        }
      }
    ];
  }
};

export const PropertiesUtils = { camelCaseToNormal, parseDataflowData, parseObligationsData };
