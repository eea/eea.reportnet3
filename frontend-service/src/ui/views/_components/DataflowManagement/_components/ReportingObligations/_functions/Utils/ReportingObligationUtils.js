import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import moment from 'moment';

const filteredInitialValues = (data, checkedId, format) => {
  let obligationList = data;
  const orderedData = onOrderCheckedValue(data, checkedId);

  if (!isNil(orderedData)) obligationList = orderedData;

  return parseObligationData(obligationList, format);
};

const initialValues = (data, format) => parseObligationData(data, format);

const isFiltered = filterByData => {
  if (!isNil(filterByData)) {
    const filteredValues = Object.values(filterByData);
    return filteredValues.every(data => isEmpty(data));
  }
  return true;
};

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
    dueDate: !isNil(data.expirationDate) ? moment(data.expirationDate).format(format) : '-',
    id: data.obligationId,
    legalInstrument: !isNil(data.legalInstruments) && data.legalInstruments.alias,
    title: data.title
  }));
};

export const ReportingObligationUtils = { filteredInitialValues, initialValues, isFiltered };
