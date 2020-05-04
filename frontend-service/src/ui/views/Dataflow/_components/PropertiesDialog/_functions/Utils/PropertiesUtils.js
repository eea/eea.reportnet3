import isNil from 'lodash/isNil';
import moment from 'moment';

const camelCaseToNormal = str => str.replace(/([A-Z])/g, ' $1').replace(/^./, str2 => str2.toUpperCase());

const parseObligationsData = (data, format) => {
  if (data.obligations) {
    return [
      {
        label: 'legalInstrument',
        data: {
          alias: data.obligations.legalInstruments.alias,
          title: data.obligations.legalInstruments.title
        }
      },
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
      }
    ];
  }
};

export const PropertiesUtils = { camelCaseToNormal, parseObligationsData };
