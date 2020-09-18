const checkValidCoordinates = coordinates => {
  if (!Array.isArray(coordinates)) {
    if (coordinates.indexOf(',') === -1) return false;
  }
  let isValid = true;
  const splittedCoordinates = Array.isArray(coordinates) ? coordinates : coordinates.split(',');
  splittedCoordinates.forEach(coordinate => {
    if (coordinate.toString().trim() === '') isValid = false;
  });
  return isValid;
};

const latLngToLngLat = (coordinates = []) =>
  typeof coordinates[0] === 'number'
    ? [coordinates[0], coordinates[1]]
    : [parseFloat(coordinates[0]), parseFloat(coordinates[1])];

const lngLatToLatLng = (coordinates = []) =>
  typeof coordinates[0] === 'number'
    ? [coordinates[1], coordinates[0]]
    : [parseFloat(coordinates[1]), parseFloat(coordinates[0])];

const parseCoordinates = (coordinates = [], parseToFloat = true) =>
  parseToFloat ? [parseFloat(coordinates[0]), parseFloat(coordinates[1])] : coordinates;

export const MapUtils = {
  checkValidCoordinates,
  latLngToLngLat,
  lngLatToLatLng,
  parseCoordinates
};
