import { Representative } from 'entities/Representative';

const parseRepresentativeListDTO = representativesDTO =>
  representativesDTO?.map(representativeDTO => parseRepresentativeDTO(representativeDTO));

const parseRepresentativeDTO = representativeDTO => {
  return new Representative({
    dataProviderGroupId: representativeDTO.dataProviderGroupId,
    dataProviderId: representativeDTO.dataProviderId,
    hasDatasets: representativeDTO.hasDatasets,
    id: representativeDTO.id,
    isReceiptDownloaded: representativeDTO.receiptDownloaded,
    isReceiptOutdated: representativeDTO.receiptOutdated,
    leadReporters: parseLeadReporters(representativeDTO.leadReporters),
    restrictFromPublic: representativeDTO.restrictFromPublic
  });
};

const parseLeadReporters = leadReporters =>
  leadReporters?.map(leadReporter => ({
    account: leadReporter.email,
    id: leadReporter.id,
    representativeId: leadReporter.representativeId
  }));

export const RepresentativeUtils = {
  parseRepresentativeListDTO
};
