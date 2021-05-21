export const UpdateLeadReporter = ({ representativeRepository }) => async (
  leadReporterAccount,
  leadReporterId,
  representativeId,
  dataflowId
) => representativeRepository.updateLeadReporter(leadReporterAccount, leadReporterId, representativeId, dataflowId);
