import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

const filteredInitialValues = (data, checkedId) => {
  let obligationList = data;
  const orderedData = onOrderCheckedValue(data, checkedId);

  if (!isNil(orderedData)) obligationList = orderedData;

  return obligationList.map(obligation => ({
    id: obligation.obligationId,
    title: obligation.title,
    legalInstrument: !isNil(obligation.legalInstruments) && obligation.legalInstruments.alias,
    dueDate: obligation.expirationDate
  }));
};

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

export const ReportingObligationUtils = { filteredInitialValues, isFiltered };
