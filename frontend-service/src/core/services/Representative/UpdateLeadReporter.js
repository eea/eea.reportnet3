export const UpdateLeadReporter = ({ representativeRepository }) => async (
  leadReporterAccount,
  leadReporterId,
  representativeId
) => representativeRepository.updateLeadReporter(leadReporterAccount, leadReporterId, representativeId);
