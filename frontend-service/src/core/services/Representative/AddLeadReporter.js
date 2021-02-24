export const AddLeadReporter = ({ representativeRepository }) => async (leadReporterAccount, representativeId) =>
  representativeRepository.addLeadReporter(leadReporterAccount, representativeId);
