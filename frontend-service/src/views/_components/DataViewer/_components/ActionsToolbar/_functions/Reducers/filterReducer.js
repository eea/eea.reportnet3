export const filterReducer = (state, { type, payload }) => {
  switch (type) {
    case 'INIT_FILTERS':
      return { ...state, visibilityDropdown: payload.dropdownFilter, validationDropdown: payload.levelErrors };

    case 'SET_VALIDATION_GROUPED_FILTER':
      return { ...state, groupedFilter: payload.groupedFilter };

    case 'SET_VALUE_FILTER':
      return { ...state, valueFilter: payload };

    case 'SET_VISIBILITY_FILTER':
      return { ...state, visibilityDropdown: payload.dropdownFilter };

    case 'SET_FILTER_ICON':
      const isFiltered = payload.currentVisibleColumns.length < payload.originalColumns.length;
      return { ...state, visibilityColumnIcon: isFiltered ? 'eye-slash' : 'eye' };

    default:
      return state;
  }
};
