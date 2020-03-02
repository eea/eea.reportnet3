export const filterReducer = (state, { type, payload }) => {
  const isFiltered = (originalFilter, filter) => {
    return filter.length < originalFilter.length;
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
        validationDropdown: payload.levelErrors
      };
    case 'SET_VISIBILITY_FILTER':
      return {
        ...state,
        visibilityDropdown: payload.dropdownFilter
      };
    case 'SET_FILTER_ICON':
      if (isFiltered(payload.originalColumns, payload.currentVisibleColumns)) {
        return { ...state, visibilityColumnIcon: 'eye-slash' };
      } else {
        return { ...state, visibilityColumnIcon: 'eye' };
      }

    default:
      return state;
  }
};
