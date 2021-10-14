import dayjs from 'dayjs';

import camelCase from 'lodash/camelCase';
import isNil from 'lodash/isNil';

import { config } from 'conf';

import { DataCollectionUtils } from 'services/_utils/DataCollectionUtils';
import { DatasetUtils } from 'services/_utils/DatasetUtils';
import { DocumentUtils } from 'services/_utils/DocumentUtils';
import { EUDatasetUtils } from 'services/_utils/EUDatasetUtils';
import { ObligationUtils } from 'services/_utils/ObligationUtils';
import { RepresentativeUtils } from 'services/_utils/RepresentativeUtils';
import { WebLinksUtils } from 'services/_utils/WebLinksUtils';

import { Dataflow } from 'entities/Dataflow';

import { UserRoleUtils } from 'repositories/_utils/UserRoleUtils';

const sortDataflowsByExpirationDate = dataflows =>
  dataflows.sort((a, b) => {
    const deadline_1 = a.expirationDate;
    const deadline_2 = b.expirationDate;
    return deadline_1 < deadline_2 ? -1 : deadline_1 > deadline_2 ? 1 : 0;
  });

const parseDataflowCount = (dataflowCountDTO, dataflowType) => {
  const dataflowCount = { reporting: 0, business: 0, citizenScience: 0, reference: 0 };

  dataflowCountDTO.forEach(dataflowType => {
    dataflowCount[camelCase(dataflowType.type)] = dataflowType.amount;
  });
  return dataflowCount;
};

const parseDataflowListDTO = dataflowsDTO => dataflowsDTO?.map(dataflowDTO => parseDataflowDTO(dataflowDTO));

const parseSortedDataflowListDTO = dataflowDTOs => {
  const dataflows = dataflowDTOs?.map(dataflowDTO => parseDataflowDTO(dataflowDTO));
  return sortDataflowsByExpirationDate(dataflows);
};

const parsePublicDataflowListDTO = dataflowsDTO =>
  dataflowsDTO?.map(dataflowDTO => parsePublicDataflowDTO(dataflowDTO));

const parsePublicDataflowDTO = publicDataflowDTO =>
  new Dataflow({
    datasets: DatasetUtils.parseDatasetListDTO(publicDataflowDTO.reportingDatasets),
    description: publicDataflowDTO.description,
    documents: DocumentUtils.parseDocumentListDTO(publicDataflowDTO.documents),
    expirationDate:
      publicDataflowDTO.deadlineDate > 0 ? dayjs(publicDataflowDTO.deadlineDate).format('YYYY-MM-DD') : '-',
    id: publicDataflowDTO.id,
    manualAcceptance: publicDataflowDTO.manualAcceptance,
    name: publicDataflowDTO.name,
    obligation: ObligationUtils.parseObligation(publicDataflowDTO.obligation),
    referenceDatasets: DatasetUtils.parseDatasetListDTO(publicDataflowDTO.referenceDatasets),
    reportingDatasetsStatus: publicDataflowDTO.reportingStatus,
    status: publicDataflowDTO.status === config.dataflowStatus.OPEN && publicDataflowDTO.releasable ? 'open' : 'closed',
    type: publicDataflowDTO.type,
    webLinks: WebLinksUtils.parseWebLinkListDTO(publicDataflowDTO.weblinks)
  });

const parseDataflowDTO = dataflowDTO =>
  new Dataflow({
    anySchemaAvailableInPublic: dataflowDTO.anySchemaAvailableInPublic,
    creationDate: dataflowDTO.creationDate > 0 ? dayjs(dataflowDTO.creationDate).format('YYYY-MM-DD') : '-',
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
    reportingDatasetsStatus: dataflowDTO.reportingStatus,
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
        allDataflowsUserListDTO[dataflowIndex].users[usersIndex].roles[roleIndex] = UserRoleUtils.getUserRoleLabel(
          role
        );
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

const parseDataProvidersUserList = usersListDTO => {
  usersListDTO.forEach((user, usersIndex) => {
    user.roles.forEach((role, roleIndex) => {
      usersListDTO[usersIndex].roles[roleIndex] = UserRoleUtils.getUserRoleLabel(role);
    });
  });
  const usersList = [];
  usersListDTO.forEach(parsedUser => {
    const { dataProviderName, email, roles } = parsedUser;

    roles.forEach(role => {
      usersList.push({ dataProviderName, email, role });
    });
  });
  usersList.forEach(user => {
    if (isNil(user.dataProviderName)) {
      user.dataProviderName = '';
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

const getTechnicalAcceptanceStatus = (datasetsStatus = []) => {
  if (datasetsStatus.some(status => status === config.datasetStatus.CORRECTION_REQUESTED.key))
    return config.datasetStatus.CORRECTION_REQUESTED.label;

  if (datasetsStatus.some(status => status === config.datasetStatus.FINAL_FEEDBACK.key))
    return config.datasetStatus.FINAL_FEEDBACK.label;

  if (datasetsStatus.every(status => status === config.datasetStatus.TECHNICALLY_ACCEPTED.key))
    return config.datasetStatus.TECHNICALLY_ACCEPTED.label;
};

const parseDatasetsInfoDTO = datasetsDTO => {
  return datasetsDTO.map(datasetDTO => {
    return {
      dataProviderCode: datasetDTO.dataProviderCode,
      dataProviderName: datasetDTO.dataProviderName,
      id: datasetDTO.id,
      name: datasetDTO.dataSetName,
      type: getDatasetType(datasetDTO.datasetTypeEnum)
    };
  });
};

const getDatasetType = datasetType => config.datasetType.find(type => type.key === datasetType)?.value;

export const DataflowUtils = {
  getTechnicalAcceptanceStatus,
  parseAllDataflowsUserList,
  parseDataflowCount,
  parseDataflowDTO,
  parseDataflowListDTO,
  parseDataProvidersUserList,
  parseDatasetsInfoDTO,
  parsePublicDataflowDTO,
  parsePublicDataflowListDTO,
  parseSortedDataflowListDTO,
  parseUsersList,
  sortDataflowsByExpirationDate
};
