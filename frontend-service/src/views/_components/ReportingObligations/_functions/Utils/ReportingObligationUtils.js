import isNil from 'lodash/isNil';
import dayjs from 'dayjs';

const filteredInitialValues = (data, checkedId, format) => {
  let obligationList = data;
  const orderedData = onOrderCheckedValue(data, checkedId);

  if (!isNil(orderedData)) obligationList = orderedData;

  return parseObligationData(obligationList, format);
};

const initialValues = (data, format) => parseObligationData(data, format);

const onOrderCheckedValue = (data, checkedId) => {
  for (let i = 0; i < data.length; i++) {
    if (data[i].obligationId === checkedId) {
      let obligation = data.splice(i, 1);
      data.unshift(obligation[0]);
      return data;
    }
  }
};

const parseObligationData = (data, format) => {
  return data.map(data => ({
    id: data.obligationId,
    title: data.title,
    legalInstrument: !isNil(data.legalInstrument) && data.legalInstrument.alias,
    dueDate: !isNil(data.expirationDate) ? dayjs(data.expirationDate).format(format) : '-'
  }));
};

export const ReportingObligationUtils = { filteredInitialValues, initialValues };
