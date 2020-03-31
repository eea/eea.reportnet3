import isNil from 'lodash/isNil';

const filteredInitialValues = data =>
  data.map(obligation => ({
    id: obligation.obligationId,
    title: obligation.title,
    legalInstrument: !isNil(obligation.legalInstruments) && obligation.legalInstruments.alias,
    dueDate: obligation.expirationDate
  }));

export const ReportingObligationUtils = { filteredInitialValues };
