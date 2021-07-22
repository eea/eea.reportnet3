import { useEffect, useReducer } from 'react';

import capitalize from 'lodash/capitalize';
import isEmpty from 'lodash/isEmpty';

const useStatusFilter = dataArray => {
  const initialState = {
    dashboardData: {},
    filterStatus: [],
    originalData: dataArray
  };

  useEffect(() => {
    statusDispatcher({ type: 'INIT_DATA', payload: dataArray });
  }, [dataArray]);

  const showArrayItem = (array, item) => {
    return !array.includes(item);
  };

  const onFilteringData = (originalData, payload) => {
    if (isEmpty(originalData)) {
      return;
    }

    const capitalizedArray = payload.map(label => capitalize(label));
    let tablesData = originalData.datasets.filter(table => showArrayItem(capitalizedArray, capitalize(table.label)));

    return { labels: originalData.labels, datasets: tablesData };
  };

  const reducer = (state, { type, payload }) => {
    let payloadLabelsArr = [];
    let filteredStatusData;
    switch (type) {
      case 'INIT_DATA':
        return {
          ...state,
          originalData: payload,
          dashboardData: payload
        };

      case 'CHECKBOX_ON':
        payloadLabelsArr = state.filterStatus.filter(status => status !== payload.label);
        filteredStatusData = onFilteringData(state.originalData, payloadLabelsArr);
        return {
          ...state,
          filterStatus: payloadLabelsArr,
          dashboardData: filteredStatusData
        };

      case 'CHECKBOX_OFF':
        payloadLabelsArr = [...state.filterStatus, payload.label];
        filteredStatusData = onFilteringData(state.originalData, payloadLabelsArr);
        return {
          ...state,
          filterStatus: payloadLabelsArr,
          dashboardData: filteredStatusData
        };
      default:
        return state;
    }
  };

  const [updatedState, statusDispatcher] = useReducer(reducer, initialState);

  return { statusDispatcher, updatedState };
};

export { useStatusFilter };
