export const DeleteLeadReporter = ({ representativeRepository }) => async (leadReporterId, dataflowId) =>
  representativeRepository.deleteLeadReporter(leadReporterId, dataflowId);
