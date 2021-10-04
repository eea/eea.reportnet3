import dayjs from 'dayjs';

import { DataflowUtils } from 'services/_utils/DataflowUtils';
import { ObligationUtils } from 'services/_utils/ObligationUtils';

import { BusinessDataflow } from 'entities/BusinessDataflow';

const parseSortedBusinessDataflowListDTO = businessDataflowDTOs => {
  const businessDataflows = businessDataflowDTOs?.map(dataflowDTO => parseBusinessDataflowDTO(dataflowDTO));
  return DataflowUtils.sortDataflowsByExpirationDate(businessDataflows);
};

const parseBusinessDataflowDTO = businessDataflowDTO =>
  new BusinessDataflow({
    creationDate:
      businessDataflowDTO.creationDate > 0 ? dayjs(businessDataflowDTO.creationDate).format('YYYY-MM-DD') : '-',
    description: businessDataflowDTO.description,
    expirationDate:
      businessDataflowDTO.deadlineDate > 0 ? dayjs(businessDataflowDTO.deadlineDate).format('YYYY-MM-DD') : '-',
    id: businessDataflowDTO.id,
    isReleasable: businessDataflowDTO.releasable,
    name: businessDataflowDTO.name,
    obligation: ObligationUtils.parseObligation(businessDataflowDTO.obligation),
    status: businessDataflowDTO.status,
    type: businessDataflowDTO.type,
    userRole: businessDataflowDTO.userRole
  });

export const BusinessDataflowUtils = {
  parseSortedBusinessDataflowListDTO
};
