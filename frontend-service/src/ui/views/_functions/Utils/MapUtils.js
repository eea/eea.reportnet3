const latLngToLngLat = (coordinates = []) =>
  typeof coordinates[0] === 'number'
    ? [coordinates[0], coordinates[1]]
    : [parseFloat(coordinates[0]), parseFloat(coordinates[1])];

const lngLatToLatLng = (coordinates = []) =>
  typeof coordinates[0] === 'number'
    ? [coordinates[1], coordinates[0]]
    : [parseFloat(coordinates[1]), parseFloat(coordinates[0])];

const parseCoordinatesToFloat = (coordinates = []) => [parseFloat(coordinates[0]), parseFloat(coordinates[1])];

export const MapUtils = {
  latLngToLngLat,
  lngLatToLatLng,
  parseCoordinatesToFloat
};
