import dayjs from 'dayjs';
import isNil from 'lodash/isNil';

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
          shortName: isNil(data.obligations.legalInstrument) ? '' : data.obligations.legalInstrument.alias,
          legalName: isNil(data.obligations.legalInstrument) ? '' : data.obligations.legalInstrument.title
        }
      }
    ];
  }
};

export const PropertiesUtils = { parseObligationsData };
