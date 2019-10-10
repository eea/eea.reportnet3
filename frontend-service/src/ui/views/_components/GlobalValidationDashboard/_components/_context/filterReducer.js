import { isEmpty } from 'lodash';

function cleanOutFilteredTableData(tablesData, labelsPositionsInFilteredLabelsArray) {
  return tablesData.map(table => ({
    ...table,
    data: table.data.filter((d, i) => !labelsPositionsInFilteredLabelsArray.includes(i)),
    totalData: table.totalData.filter((td, i) => !labelsPositionsInFilteredLabelsArray.includes(i))
  }));
}

function getLabelIndex(originalData, label) {
  return originalData.labels.indexOf(label);
}

function showArrayItem(array, item) {
  return !array.includes(item);
}

const onFilteringData = (originalData, datasetsIdsArr, reportersLabelsArr, msgStatusTypesArr) => {
  console.log(originalData, datasetsIdsArr, reportersLabelsArr, msgStatusTypesArr);
  if (isEmpty(originalData)) {
    return;
  }

  let tablesData = originalData.datasets.filter(table => showArrayItem(datasetsIdsArr, table.tableId));

  const labels = originalData.labels.filter(label => showArrayItem(reportersLabelsArr, label));
  const labelsPositionsInFilteredLabelsArray = reportersLabelsArr.map(label => getLabelIndex(originalData, label));

  tablesData = cleanOutFilteredTableData(tablesData, labelsPositionsInFilteredLabelsArray);

  tablesData = tablesData.filter(table => showArrayItem(msgStatusTypesArr, table.label));
  return { labels: labels, datasets: tablesData };
};

export const filterReducer = (state, { type, payload }) => {
  let reportersLabelsArr = [];
  let tablesIdsArray = [];
  let msgStatusTypesArray = [];
  let filteredTableData;
  console.log(type, payload, state);
  switch (type) {
    case 'INIT_DATA':
      return {
        ...state,
        originalData: payload,
        data: payload
      };
    case 'APPLY_FILTERS':
      return {
        ...state,
        data: { ...payload.originalData },
        reporterFilter: payload.reporterFilter,
        statusFilter: payload.statusFilter,
        tableFilter: payload.tableFilter
      };
    case 'TABLE_CHECKBOX_ON':
      tablesIdsArray = state.tableFilter.filter(table => table !== payload.tableId);
      filteredTableData = onFilteringData(state.originalData, tablesIdsArray, state.reporterFilter, state.statusFilter);

      return {
        ...state,
        tableFilter: tablesIdsArray,
        data: filteredTableData
      };

    case 'TABLE_CHECKBOX_OFF':
      tablesIdsArray = [...state.tableFilter, payload.tableId];

      filteredTableData = onFilteringData(state.originalData, tablesIdsArray, state.reporterFilter, state.statusFilter);

      return {
        ...state,
        tableFilter: tablesIdsArray,
        data: filteredTableData
      };

    case 'REPORTER_CHECKBOX_ON':
      reportersLabelsArr = state.reporterFilter.filter(label => label !== payload.label);

      filteredTableData = onFilteringData(
        state.originalData,
        state.tableFilter,
        reportersLabelsArr,
        state.statusFilter
      );

      return {
        ...state,
        reporterFilter: reportersLabelsArr,
        data: filteredTableData
      };

    case 'REPORTER_CHECKBOX_OFF':
      reportersLabelsArr = [...state.reporterFilter, payload.label];

      filteredTableData = onFilteringData(
        state.originalData,
        state.tableFilter,
        reportersLabelsArr,
        state.statusFilter
      );
      return {
        ...state,
        reporterFilter: reportersLabelsArr,
        data: filteredTableData
      };
    case 'STATUS_FILTER_ON':
      msgStatusTypesArray = state.statusFilter.filter(status => status !== payload.msg);

      filteredTableData = onFilteringData(
        state.originalData,
        state.tableFilter,
        state.reporterFilter,
        msgStatusTypesArray
      );

      return {
        ...state,
        statusFilter: msgStatusTypesArray,
        data: filteredTableData
      };
    case 'STATUS_FILTER_OFF':
      msgStatusTypesArray = [...state.statusFilter, payload.msg];

      filteredTableData = onFilteringData(
        state.originalData,
        state.tableFilter,
        state.reporterFilter,
        msgStatusTypesArray
      );

      return {
        ...state,
        statusFilter: msgStatusTypesArray,
        data: filteredTableData
      };

    default:
      return state;
  }
};
