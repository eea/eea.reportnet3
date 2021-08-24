import capitalize from 'lodash/capitalize';
import dayjs from 'dayjs';
import isNil from 'lodash/isNil';

import { config } from 'conf';

import { DataCollectionUtils } from 'services/_utils/DataCollectionUtils';
import { DatasetUtils } from 'services/_utils/DatasetUtils';
import { DocumentUtils } from 'services/_utils/DocumentUtils';
import { EUDatasetUtils } from 'services/_utils/EUDatasetUtils';
import { WebLinksUtils } from 'services/_utils/WebLinksUtils';
import { ObligationUtils } from 'services/_utils/ObligationUtils';
import { RepresentativeUtils } from 'services/_utils/RepresentativeUtils';

import { Dataflow } from 'entities/Dataflow';

import { UserRoleUtils } from 'repositories/_utils/UserRoleUtils';

const sortDataflowsByExpirationDate = dataflows =>
  dataflows.sort((a, b) => {
    const deadline_1 = a.expirationDate;
    const deadline_2 = b.expirationDate;
    return deadline_1 < deadline_2 ? -1 : deadline_1 > deadline_2 ? 1 : 0;
  });

const parseDataflowListDTO = dataflowsDTO => dataflowsDTO?.map(dataflowDTO => parseDataflowDTO(dataflowDTO));

const parsePublicDataflowListDTO = dataflowsDTO =>
  dataflowsDTO?.map(dataflowDTO => parsePublicDataflowDTO(dataflowDTO));

const parseSortedDataflowListDTO = dataflowDTOs => {
  const dataflows = dataflowDTOs?.map(dataflowDTO => parseDataflowDTO(dataflowDTO));
  return sortDataflowsByExpirationDate(dataflows);
};

const parsePublicDataflowDTO = publicDataflowDTO =>
  new Dataflow({
    datasets: DatasetUtils.parseDatasetListDTO(publicDataflowDTO.reportingDatasets),
    description: publicDataflowDTO.description,
    documents: DocumentUtils.parseDocumentListDTO(publicDataflowDTO.documents),
    expirationDate:
      publicDataflowDTO.deadlineDate > 0 ? dayjs(publicDataflowDTO.deadlineDate).format('YYYY-MM-DD') : '-',
    id: publicDataflowDTO.id,
    isReleasable: publicDataflowDTO.releasable,
    manualAcceptance: publicDataflowDTO.manualAcceptance,
    name: publicDataflowDTO.name,
    obligation: ObligationUtils.parseObligation(publicDataflowDTO.obligation),
    referenceDatasets: DatasetUtils.parseDatasetListDTO(publicDataflowDTO.referenceDatasets),
    reportingDatasetsStatus: capitalize(publicDataflowDTO.reportingStatus?.replaceAll('_', ' ')),
    status: publicDataflowDTO.status === config.dataflowStatus.OPEN ? 'OPEN' : 'CLOSED'
  });

const parseDataflowDTO = dataflowDTO =>
  new Dataflow({
    anySchemaAvailableInPublic: dataflowDTO.anySchemaAvailableInPublic,
    creationDate: dataflowDTO.creationDate,
    dataCollections: DataCollectionUtils.parseDataCollectionListDTO(dataflowDTO.dataCollections),
    dataProviderGroupId: dataflowDTO.dataProviderGroupId,
    dataProviderGroupName: dataflowDTO.dataProviderGroupName,
    datasets: DatasetUtils.parseDatasetListDTO(dataflowDTO.reportingDatasets),
    description: dataflowDTO.description,
    designDatasets: DatasetUtils.parseDatasetListDTO(dataflowDTO.designDatasets),
    documents: DocumentUtils.parseDocumentListDTO(dataflowDTO.documents),
    euDatasets: EUDatasetUtils.parseEUDatasetListDTO(dataflowDTO.euDatasets),
    expirationDate: dataflowDTO.deadlineDate > 0 ? dayjs(dataflowDTO.deadlineDate).format('YYYY-MM-DD') : '-',
    fmeUserId: dataflowDTO.fmeUserId,
    fmeUserName: dataflowDTO.fmeUserName,
    id: dataflowDTO.id,
    isReleasable: dataflowDTO.releasable,
    manualAcceptance: dataflowDTO.manualAcceptance,
    name: dataflowDTO.name,
    obligation: ObligationUtils.parseObligation(dataflowDTO.obligation),
    referenceDatasets: DatasetUtils.parseDatasetListDTO(dataflowDTO.referenceDatasets),
    reportingDatasetsStatus: capitalize(dataflowDTO.reportingStatus?.replaceAll('_', ' ')),
    representatives: RepresentativeUtils.parseRepresentativeListDTO(dataflowDTO.representatives),
    requestId: dataflowDTO.requestId,
    showPublicInfo: dataflowDTO.showPublicInfo,
    status: dataflowDTO.status,
    testDatasets: DatasetUtils.parseDatasetListDTO(dataflowDTO.testDatasets),
    type: dataflowDTO.type,
    userRole: dataflowDTO.userRole,
    webLinks: WebLinksUtils.parseWebLinkListDTO(dataflowDTO.weblinks)
  });

const parseAllDataflowsUserList = allDataflowsUserListDTO => {
  allDataflowsUserListDTO.forEach((dataflow, dataflowIndex) => {
    dataflow.users.forEach((user, usersIndex) => {
      user.roles.forEach((role, roleIndex) => {
        allDataflowsUserListDTO[dataflowIndex].users[usersIndex].roles[roleIndex] =
          UserRoleUtils.getUserRoleLabel(role);
      });
    });
  });
  const usersList = [];
  allDataflowsUserListDTO.forEach(dataflow => {
    const { dataflowId, dataflowName } = dataflow;
    dataflow.users.forEach(parsedUser => {
      const { email, roles } = parsedUser;
      roles.forEach(role => {
        usersList.push({ dataflowId, dataflowName, email, role });
      });
    });
  });
  return usersList;
};

const parseCountriesUserList = usersListDTO => {
  usersListDTO.forEach((user, usersIndex) => {
    user.roles.forEach((role, roleIndex) => {
      usersListDTO[usersIndex].roles[roleIndex] = UserRoleUtils.getUserRoleLabel(role);
    });
  });
  const usersList = [];
  usersListDTO.forEach(parsedUser => {
    const { country, email, roles } = parsedUser;
    roles.forEach(role => {
      usersList.push({ country, email, role });
    });
  });
  usersList.forEach(user => {
    if (isNil(user.country)) {
      user.country = '';
    }
  });
  return usersList;
};

const parseUsersList = usersListDTO => {
  usersListDTO.forEach((user, usersIndex) => {
    user.roles.forEach((role, roleIndex) => {
      usersListDTO[usersIndex].roles[roleIndex] = UserRoleUtils.getUserRoleLabel(role);
    });
  });
  const usersList = [];
  usersListDTO.forEach(parsedUser => {
    const { email, roles } = parsedUser;
    roles.forEach(role => {
      usersList.push({ email, role });
    });
  });
  return usersList;
};

export const DataflowUtils = {
  sortDataflowsByExpirationDate,
  parseDataflowDTO,
  parseDataflowListDTO,
  parsePublicDataflowListDTO,
  parsePublicDataflowDTO,
  parseSortedDataflowListDTO,
  parseAllDataflowsUserList,
  parseCountriesUserList,
  parseUsersList
};
