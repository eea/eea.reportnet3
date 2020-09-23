import isNil from 'lodash/isNil';

const checkRSID = rsid => {
  switch (rsid.split(':')[1].trim()) {
    case '4326':
    case '4258':
    case '3035':
      return rsid;
    default:
      return 'EPSG:4326';
  }
};
const checkValidCoordinates = coordinates => {
  if (coordinates === '') return false;
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

const isValidJSON = value => {
  if (value.indexOf('{') === -1) {
    return false;
  }
  try {
    JSON.parse(value);
  } catch (e) {
    return false;
  }
  return true;
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

const parseGeometryData = records => {
  const baseJson = `{"type": "Feature", "geometry": {"type":"Point","coordinates":[55.6811608,12.5844761]}, "properties": {"rsid": "EPSG:4326"}}`;
  records.forEach(record =>
    record.dataRow.forEach(row => {
      if (row.fieldData.type === 'POINT') {
        const parsedJSON = JSON.parse(baseJson);
        const value = !isNil(row.fieldData[Object.keys(row.fieldData)[0]])
          ? row.fieldData[Object.keys(row.fieldData)[0]]
          : ',';
        const splittedValue = value.split('-');
        if (!checkValidCoordinates(splittedValue.length === 0 ? value : splittedValue[0])) {
          parsedJSON.geometry.coordinates = [''];
        } else {
          parsedJSON.geometry.coordinates =
            splittedValue.length === 0
              ? [value.replace(', ', ',').split(',')[0], value.replace(', ', ',').split(',')[1]]
              : [splittedValue[0].replace(', ', ',').split(',')[0], splittedValue[0].replace(', ', ',').split(',')[1]];
          parsedJSON.properties.rsid = splittedValue.length === 0 ? '4326' : checkRSID(splittedValue[1]);
        }

        row.fieldData[Object.keys(row.fieldData)[0]] = JSON.stringify(parsedJSON);
      }
    })
  );
  return records;
};

export const MapUtils = {
  checkValidCoordinates,
  isValidJSON,
  latLngToLngLat,
  lngLatToLatLng,
  parseCoordinates,
  parseGeometryData
};
