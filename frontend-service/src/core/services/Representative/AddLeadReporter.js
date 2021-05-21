export const AddLeadReporter = ({ representativeRepository }) => async (
  leadReporterAccount,
  representativeId,
  dataflowId
) => representativeRepository.addLeadReporter(leadReporterAccount, representativeId, dataflowId);
