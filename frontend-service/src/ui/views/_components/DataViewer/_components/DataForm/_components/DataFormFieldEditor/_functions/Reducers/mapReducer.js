export const mapReducer = (state, { type, payload }) => {
  switch (type) {
    case 'CANCEL_SAVE_MAP_NEW_POINT':
      return {
        ...state,
        isMapOpen: false,
        newPoint: '',
        newPointCRS: payload.newPointCRS
      };
    case 'DISPLAY_COORDINATE_ERROR':
      return {
        ...state,
        showCoordinateError: payload
      };
    case 'OPEN_MAP':
      return {
        ...state,
        isMapOpen: true,
        mapCoordinates: payload.coordinates,
        newPointCRS: state.currentCRS,
        newPoint: ''
      };
    case 'SAVE_MAP_COORDINATES':
      return { ...state, isMapOpen: false, currentCRS: payload.crs, isMapDisabled: false, newPoint: '' };
    case 'SET_MAP_NEW_POINT':
      return {
        ...state,
        newPoint: payload.coordinates,
        newPointCRS: payload.filteredCRS
      };
    case 'SET_MAP_CRS':
      return {
        ...state,
        currentCRS: payload.crs
      };
    case 'TOGGLE_MAP_VISIBILITY':
      return { ...state, isMapOpen: payload };
    case 'TOGGLE_MAP_DISABLED':
      return { ...state, isMapDisabled: payload };
    default:
      return state;
  }
};
