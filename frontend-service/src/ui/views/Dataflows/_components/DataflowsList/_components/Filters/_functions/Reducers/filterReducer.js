import { filterUtils } from '../Utils/filterUtils';
import isEmpty from 'lodash/isEmpty';

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
    Object.keys(state.filterBy).filter(
      key => key !== payload.filter && key !== 'status' && key !== 'userRole' && key !== 'expirationDate'
    );

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

  const onApplyFilters = (filteredKeys, i) => [
    ...payload.data.filter(data => {
      if (state.selectOptions.includes(payload.filter)) {
        return (
          [...payload.value.map(type => type.value.toLowerCase())].includes(data[payload.filter].toLowerCase()) &&
          checkFilters(filteredKeys, data)
        );
      } else if (state.dateOptions.includes(payload.filter)) {
        const dates = [];
        payload.value.map(date => dates.push(new Date(date).getTime() / 1000));

        if (!dates.includes(0)) {
          return (
            new Date(data[payload.filter]).getTime() / 1000 >= dates[0] &&
            new Date(data[payload.filter]).getTime() / 1000 <= dates[1] &&
            checkFilters(filteredKeys, data) &&
            [...state.filterBy.status.map(status => status.value.toLowerCase())].includes(data.status.toLowerCase()) &&
            [...state.filterBy.userRole.map(userRole => userRole.value.toLowerCase())].includes(
              data.userRole.toLowerCase()
            )
          );
        } else {
          return [...state.filteredData];
        }
      } else {
        const dates = state.filterBy.expirationDate.map(date => new Date(date).getTime() / 1000);
        console.log('dates', dates);

        return (
          data[payload.filter].toLowerCase().includes(payload.value.toLowerCase()) &&
          checkFilters(filteredKeys, data) &&
          [...state.filterBy.status.map(status => status.value.toLowerCase())].includes(data.status.toLowerCase()) &&
          [...state.filterBy.userRole.map(userRole => userRole.value.toLowerCase())].includes(
            data.userRole.toLowerCase()
          )
        );
      }
    })
  ];

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
      const appliedFilters = onApplyFilters(filteredKeys);

      return {
        ...state,
        filterBy: { ...state.filterBy, [payload.filter]: payload.value },
        filteredData: appliedFilters
      };

    case 'CLEAR_ALL_FILTERS':
      return {
        ...state,
        filterBy: payload.filterBy,
        filteredData: payload.filteredData
      };

    default:
      return state;
  }
};
