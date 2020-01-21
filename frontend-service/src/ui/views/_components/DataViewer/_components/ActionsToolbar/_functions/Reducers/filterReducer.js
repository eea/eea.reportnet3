export const filterReducer = (state, { type, payload }) => {
  const isFiltered = (originalFilter, filter) => {
    if (filter.length < originalFilter.length) {
      return true;
    } else {
      return false;
    }
  };

  switch (type) {
    case 'INIT_FILTERS':
      return {
        ...state,
        visibilityDropdown: payload.dropdownFilter,
        validationDropdown: payload.levelErrors
      };
    case 'SET_VALIDATION_FILTER':
      return {
        ...state,
        visibilityDropdown: payload.dropdownFilter,
        validationDropdown: payload.levelErrors
      };
    case 'SET_FILTER_ICON':
      if (isFiltered(payload.originalColumns, payload.currentinvisibleColumns)) {
        return { ...state, visibilityColumnIcon: 'eye-slash' };
      } else {
        return { ...state, visibilityColumnIcon: 'eye' };
      }

    default:
      return state;
  }
};
