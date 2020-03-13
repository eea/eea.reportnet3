import { filterUtils } from '../Utils/filterUtils';

const onSortData = (data, order, property) => {
  if (order === 1) {
    return data.sort((a, b) => {
      const textA = a[property].toUpperCase();
      const textB = b[property].toUpperCase();
      return textA < textB ? -1 : textA > textB ? 1 : 0;
    });
  } else {
    return data.sort((a, b) => {
      const textA = a[property].toUpperCase();
      const textB = b[property].toUpperCase();
      return textA < textB ? 1 : textA > textB ? -1 : 0;
    });
  }
};

export const filterReducer = (state, { type, payload }) => {
  const getFilterKeys = () =>
    Object.keys(state.filterBy).filter(key => key !== payload.filter && key !== 'status' && key !== 'userRole');

  const checkFilters = (filteredKeys, dataflow) => {
    for (let i = 0; i < filteredKeys.length; i++) {
      if (state.filterBy[filteredKeys[i]].toLowerCase() !== '') {
        if (!dataflow[filteredKeys[i]].toLowerCase().includes(state.filterBy[filteredKeys[i]].toLowerCase())) {
          return false;
        }
      }
    }
    return true;
  };

  switch (type) {
    case 'ORDER_DATA':
      return {
        ...state,
        data: onSortData([...state.data], payload.order, payload.property),
        filteredData: onSortData([...state.filteredData], payload.order, payload.property),
        orderBy: { ...state.orderBy, [payload.property]: -payload.order }
      };

    case 'FILTER_DATA':
      const filteredKeys = getFilterKeys();
      return {
        ...state,
        filterBy: { ...state.filterBy, [payload.filter]: payload.value },
        filteredData: [
          ...payload.data.filter(data =>
            payload.filter === 'status' || payload.filter === 'userRole'
              ? [...payload.value.map(type => type.value.toLowerCase())].includes(data[payload.filter].toLowerCase()) &&
                checkFilters(filteredKeys, data)
              : data[payload.filter].toLowerCase().includes(payload.value.toLowerCase()) &&
                [...state.filterBy.status.map(status => status.value.toLowerCase())].includes(
                  data.status.toLowerCase()
                ) &&
                checkFilters(filteredKeys, data)
          )
        ]
      };

    case 'CLEAR_ALL_FILTERS':
      return {
        ...state,
        filterBy: payload.filterBy,
        filteredData: payload.filteredData
      };

    case 'CLEAR_INPUT':
      return {
        ...state,
        filterBy: { ...state.filterBy, [payload.property]: '' }
      };

    default:
      return state;
  }
};
