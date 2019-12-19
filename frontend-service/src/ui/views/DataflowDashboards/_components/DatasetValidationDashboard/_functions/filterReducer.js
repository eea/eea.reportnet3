import { isEmpty, isUndefined } from 'lodash';

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

const onFilteringReporters = (originalData, tableIds, reporters, status) => {
  if (isEmpty(originalData)) {
    return;
  }
  let tablesData = [];
  if (isUndefined(tableIds)) {
    return tablesData;
  }
  tablesData = originalData.datasets.filter(table => filterItem(tableIds, table.tableId));

  const labels = originalData.labels.filter(label => filterItem(reporters, label));
  const labelsPositionsInFilteredLabels = reporters.map(label => getLabelIndex(originalData, label));

  tablesData = cleanOutFilteredTableData(tablesData, labelsPositionsInFilteredLabels);
  tablesData = tablesData.filter(table => filterItem(status, table.label));
  return { labels: labels, datasets: tablesData };
};

const onFilteringTables = (originalData, tableIds, reporterFilters, status) => {
  let tablesData = [];
  let labels = [];
  console.log({ reporterFilters });
  // if (isUndefined(tableIds)) {
  // }
  if (isEmpty(originalData) || reporterFilters.length == originalData.labels.length) {
    console.log('tests');
    tablesData = [];
    labels = [];
  } else {
    tablesData = originalData.datasets.filter(table => filterItem(tableIds, table.tableId));
    tablesData = tablesData.filter(table => filterItem(status, table.label));
    labels = originalData.labels.filter(label => filterItem(reporterFilters, label));
  }
  console.log({ labels });
  console.log({ tablesData });
  return { labels: labels, datasets: tablesData };
};

const onFilteringStatus = (originalData, status, reporters, tableIds) => {
  if (isEmpty(originalData)) {
    return;
  }
  let tablesData = originalData.datasets.filter(table => filterItem(tableIds, table.tableId));

  const labels = originalData.labels.filter(label => filterItem(reporters, label));
  const labelsPositionsInFilteredLabels = reporters.map(label => getLabelIndex(originalData, label));

  tablesData = cleanOutFilteredTableData(tablesData, labelsPositionsInFilteredLabels);
  tablesData = tablesData.filter(table => filterItem(status, table.label));
  return { labels: labels, datasets: tablesData };
};

export const filterReducer = (state, { type, payload }) => {
  let reporters = [];
  let tablesIds = [];
  let status = [];
  let filteredTableData;
  switch (type) {
    case 'INIT_DATA':
      return {
        ...state,
        originalData: payload,
        data: payload
      };
    case 'TABLE_CHECKBOX_ON':
      console.log(state);
      tablesIds = !isUndefined(state.tableFilter) ? state.tableFilter.filter(table => table !== payload.tableId) : [];
      filteredTableData = onFilteringTables(state.originalData, tablesIds, state.reporterFilter, state.statusFilter);
      console.log({ tablesIds });
      console.log({ filteredTableData });
      return {
        ...state,
        tableFilter: tablesIds,
        data: filteredTableData
      };

    case 'TABLE_CHECKBOX_OFF':
      tablesIds = [...state.tableFilter, payload.tableId];
      filteredTableData = onFilteringTables(state.originalData, tablesIds, state.reporterFilter, state.statusFilter);

      return {
        ...state,
        tableFilter: tablesIds,
        data: filteredTableData
      };

    case 'TABLE_CHECKBOX_SELECT_ALL_ON':
      filteredTableData = onFilteringTables(state.originalData, [], state.reporterFilter, state.statusFilter);
      return {
        ...state,
        tableFilter: [],
        data: filteredTableData
      };

    case 'TABLE_CHECKBOX_SELECT_ALL_OFF':
      tablesIds = [...state.tableFilter, payload.tableId];
      return {
        ...state,
        tableFilter: tablesIds[0],
        data: []
      };

    case 'REPORTER_CHECKBOX_ON':
      reporters = state.reporterFilter.filter(label => label !== payload.label);
      filteredTableData = onFilteringReporters(state.originalData, state.tableFilter, reporters, state.statusFilter);

      return {
        ...state,
        reporterFilter: reporters,
        data: filteredTableData
      };

    case 'REPORTER_CHECKBOX_OFF':
      reporters = [...state.reporterFilter, payload.label];
      filteredTableData = onFilteringReporters(state.originalData, state.tableFilter, reporters, state.statusFilter);

      return {
        ...state,
        reporterFilter: reporters,
        data: filteredTableData
      };

    case 'REPORTER_CHECKBOX_SELECT_ALL_ON':
      filteredTableData = onFilteringReporters(state.originalData, state.tableFilter, reporters, state.statusFilter);

      return {
        ...state,
        reporterFilter: [],
        data: filteredTableData
      };

    case 'REPORTER_CHECKBOX_SELECT_ALL_OFF':
      reporters = [...state.reporterFilter, payload.allFilters];
      filteredTableData = onFilteringReporters(state.originalData, state.tableFilter, reporters, state.statusFilter);

      return {
        ...state,
        reporterFilter: reporters[0],
        data: []
      };

    case 'STATUS_FILTER_ON':
      status = state.statusFilter.filter(status => status !== payload.msg);
      filteredTableData = onFilteringStatus(state.originalData, status, state.reporterFilter, state.tableFilter);

      return {
        ...state,
        statusFilter: status,
        data: filteredTableData
      };

    case 'STATUS_FILTER_OFF':
      status = [...state.statusFilter, payload.msg];
      filteredTableData = onFilteringStatus(state.originalData, status, state.reporterFilter, state.tableFilter);
      return {
        ...state,
        statusFilter: status,
        data: filteredTableData
      };

    default:
      return state;
  }
};
