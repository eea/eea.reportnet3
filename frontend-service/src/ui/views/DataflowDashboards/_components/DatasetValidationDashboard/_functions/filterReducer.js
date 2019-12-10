import { isEmpty } from 'lodash';

const cleanOutFilteredTableData = (tablesData, labelsPositionsInFilteredLabels) => {
  return tablesData.map(table => ({
    ...table,
    data: table.data.filter((_, i) => !labelsPositionsInFilteredLabels.includes(i)),
    totalData: table.totalData.filter((_, i) => !labelsPositionsInFilteredLabels.includes(i))
  }));
};

const getLabelIndex = (originalData, label) => {
  return originalData.labels.indexOf(label);
};

const filterItem = (filter, item) => {
  return !filter.includes(item);
};

const onFilteringData = (originalData, datasetsIds, reportersLabels, msgStatusTypes) => {
  if (isEmpty(originalData)) {
    return;
  }

  let tablesData = originalData.datasets.filter(table => filterItem(datasetsIds, table.tableId));
  const labels = originalData.labels.filter(label => filterItem(reportersLabels, label));
  const labelsPositionsInFilteredLabels = reportersLabels.map(label => getLabelIndex(originalData, label));

  tablesData = cleanOutFilteredTableData(tablesData, labelsPositionsInFilteredLabels);
  tablesData = tablesData.filter(table => filterItem(msgStatusTypes, table.label));
  return { labels: labels, datasets: tablesData };
};

export const filterReducer = (state, { type, payload }) => {
  let reportersLabels = [];
  let tablesIds = [];
  let msgStatusTypes = [];
  let filteredTableData;
  switch (type) {
    case 'INIT_DATA':
      return {
        ...state,
        originalData: payload,
        data: payload
      };
    case 'TABLE_CHECKBOX_ON':
      tablesIds = state.tableFilter.filter(table => table !== payload.tableId);
      filteredTableData = onFilteringData(state.originalData, tablesIds, state.reporterFilter, state.statusFilter);

      return {
        ...state,
        tableFilter: tablesIds,
        data: filteredTableData
      };

    case 'TABLE_CHECKBOX_OFF':
      tablesIds = [...state.tableFilter, payload.tableId];

      filteredTableData = onFilteringData(state.originalData, tablesIds, state.reporterFilter, state.statusFilter);

      return {
        ...state,
        tableFilter: tablesIds,
        data: filteredTableData
      };

    case 'REPORTER_CHECKBOX_ON':
      reportersLabels = state.reporterFilter.filter(label => label !== payload.label);

      filteredTableData = onFilteringData(state.originalData, state.tableFilter, reportersLabels, state.statusFilter);

      return {
        ...state,
        reporterFilter: reportersLabels,
        data: filteredTableData
      };

    case 'REPORTER_CHECKBOX_OFF':
      reportersLabels = [...state.reporterFilter, payload.label];

      filteredTableData = onFilteringData(state.originalData, state.tableFilter, reportersLabels, state.statusFilter);
      return {
        ...state,
        reporterFilter: reportersLabels,
        data: filteredTableData
      };
    case 'STATUS_FILTER_ON':
      msgStatusTypes = state.statusFilter.filter(status => status !== payload.msg);

      filteredTableData = onFilteringData(state.originalData, state.tableFilter, state.reporterFilter, msgStatusTypes);

      return {
        ...state,
        statusFilter: msgStatusTypes,
        data: filteredTableData
      };
    case 'STATUS_FILTER_OFF':
      msgStatusTypes = [...state.statusFilter, payload.msg];
      filteredTableData = onFilteringData(state.originalData, state.tableFilter, state.reporterFilter, msgStatusTypes);
      return {
        ...state,
        statusFilter: msgStatusTypes,
        data: filteredTableData
      };

    default:
      return state;
  }
};
