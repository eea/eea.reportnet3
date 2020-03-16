import isEmpty from 'lodash/isEmpty';

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

  const checkDates = (betweenDates, data) => {
    if (!isEmpty(betweenDates)) {
      const dates = betweenDates.map(date => new Date(date).getTime() / 1000);
      return new Date(data).getTime() / 1000 >= dates[0] && new Date(data).getTime() / 1000 <= dates[1];
    }
    return true;
  };

  const onApplyFilters = (filteredKeys, i) => [
    ...payload.data.filter(data => {
      if (state.selectOptions.includes(payload.filter)) {
        return (
          [...payload.value.map(type => type.value.toLowerCase())].includes(data[payload.filter].toLowerCase()) &&
          checkFilters(filteredKeys, data) &&
          checkDates(state.filterBy.expirationDate, data.expirationDate)
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
        return (
          data[payload.filter].toLowerCase().includes(payload.value.toLowerCase()) &&
          checkFilters(filteredKeys, data) &&
          [...state.filterBy.status.map(status => status.value.toLowerCase())].includes(data.status.toLowerCase()) &&
          [...state.filterBy.userRole.map(userRole => userRole.value.toLowerCase())].includes(
            data.userRole.toLowerCase()
          ) &&
          checkDates(state.filterBy.expirationDate, data.expirationDate)
        );
      }
    })
  ];

  switch (type) {
    case 'ORDER_DATA':
      return {
        ...state,
        data: payload.data,
        filteredData: payload.filteredData,
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
