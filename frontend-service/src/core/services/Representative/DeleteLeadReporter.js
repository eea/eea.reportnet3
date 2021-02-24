export const DeleteLeadReporter = ({ representativeRepository }) => async leadReporterId =>
  representativeRepository.deleteLeadReporter(leadReporterId);
