import dayjs from 'dayjs';

import utc from 'dayjs/plugin/utc';

import { ObligationUtils } from 'services/_utils/ObligationUtils';

import { BusinessDataflow } from 'entities/BusinessDataflow';

const parseBusinessDataflows = businessDataflowDTOs =>
  businessDataflowDTOs?.map(dataflowDTO => parseBusinessDataflowDTO(dataflowDTO));

const parseBusinessDataflowDTO = businessDataflowDTO => {
  dayjs.extend(utc);

  return new BusinessDataflow({
    creationDate:
      businessDataflowDTO.creationDate > 0 ? dayjs(businessDataflowDTO.creationDate).format('YYYY-MM-DD') : '-',
    description: businessDataflowDTO.description,
    expirationDate:
      businessDataflowDTO.deadlineDate > 0 ? dayjs(businessDataflowDTO.deadlineDate).utc().format('YYYY-MM-DD') : '-',
    id: businessDataflowDTO.id,
    isReleasable: businessDataflowDTO.releasable,
    name: businessDataflowDTO.name,
    obligation: ObligationUtils.parseObligation(businessDataflowDTO.obligation),
    reportingDatasetsStatus: businessDataflowDTO.reportingStatus,
    status: businessDataflowDTO.status,
    type: businessDataflowDTO.type,
    userRole: businessDataflowDTO.userRole
  });
};

export const BusinessDataflowUtils = { parseBusinessDataflows };
