import isNil from 'lodash/isNil';
import moment from 'moment';

const camelCaseToNormal = str => str.replace(/([A-Z])/g, ' $1').replace(/^./, str2 => str2.toUpperCase());

const parseObligationsData = (data, format) => {
  if (data.obligations) {
    return [
      {
        label: 'obligation',
        data: {
          title: data.obligations.title,
          description: data.obligations.description,
          comment: data.obligations.comment,
          reportingFrequency: data.obligations.reportingFrequency,
          nextReportDue: !isNil(data.obligations.expirationDate)
            ? moment(data.obligations.expirationDate).format(format)
            : '-'
        }
      },
      {
        label: 'legalInstrument',
        data: {
          shortName: data.obligations.legalInstruments.alias,
          legalName: data.obligations.legalInstruments.title
        }
      }
    ];
  }
};

export const PropertiesUtils = { camelCaseToNormal, parseObligationsData };
