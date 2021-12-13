import { orderBy } from 'lodash';

const getActiveTab = (tabMenuItems, activeIndex) => {
  const { id, label } = tabMenuItems[activeIndex];

  return { activeTab: tabMenuItems[activeIndex], tabId: id, tabLabel: label };
};

const parseDataflows = (data, pinnedDataflows) => {
  const dataflows = data?.map(dataflow => ({
    id: dataflow.id,
    creationDate: dataflow.creationDate,
    description: dataflow.description,
    expirationDate: dataflow.expirationDate,
    legalInstrument: dataflow.obligation?.legalInstrument?.alias,
    name: dataflow.name,
    obligationTitle: dataflow.obligation?.title,
    obligationId: dataflow.obligation?.obligationId?.toString(),
    pinned: pinnedDataflows.some(pinnedDataflow => pinnedDataflow === dataflow.id.toString()) ? 'pinned' : 'unpinned',
    reportingDatasetsStatus: dataflow.reportingDatasetsStatus,
    showPublicInfo: dataflow.showPublicInfo,
    status: dataflow.status,
    statusKey: dataflow.statusKey,
    userRole: dataflow.userRole
  }));

  return sortDataflows(dataflows);
};

const sortDataflows = (dataflows = []) => {
  return orderBy(
    dataflows,
    ['pinned', 'expirationDate', 'status', 'id', 'creationDate'],
    ['asc', 'asc', 'asc', 'asc', 'asc']
  );
};

export const DataflowsUtils = { getActiveTab, parseDataflows, sortDataflows };
