import dayjs from 'dayjs';
import isNil from 'lodash/isNil';

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
            ? dayjs(data.obligations.expirationDate).format(format)
            : '-',
          id: data.obligations.obligationId
        }
      },
      {
        label: 'legalInstrument',
        data: {
          shortName: isNil(data.obligations.legalInstruments) ? '' : data.obligations.legalInstruments.alias,
          legalName: isNil(data.obligations.legalInstruments) ? '' : data.obligations.legalInstruments.title
        }
      }
    ];
  }
};

export const PropertiesUtils = { camelCaseToNormal, parseObligationsData };
