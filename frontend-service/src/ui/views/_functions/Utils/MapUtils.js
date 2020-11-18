import first from 'lodash/first';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

const changeIncorrectCoordinates = record => {
  const baseJson = `{"type": "Feature", "geometry": {"type":"Point","coordinates":[]}, "properties": {"srid": "EPSG:4326"}}`;
  record.dataRow.forEach(row => {
    if (row.fieldData.type === 'POINT') {
      const parsedJSON = JSON.parse(
        !isNil(Object.values(row.fieldData)[0]) ? Object.values(row.fieldData)[0] : baseJson
      );
      const value = !isNil(row.fieldData[Object.keys(row.fieldData)[0]])
        ? row.fieldData[Object.keys(row.fieldData)[0]]
        : ',';
      if (!checkValidJSONCoordinates(value)) parsedJSON.geometry.coordinates = [''];
      if (!checkSRID(parsedJSON.properties.srid)) parsedJSON.properties.srid = '4326';

      row.fieldData[Object.keys(row.fieldData)[0]] = JSON.stringify(parsedJSON);
    }
  });
  return record;
};

const checkSRID = srid => {
  switch (srid.split(':')[1].trim()) {
    case '4326':
    case '4258':
    case '3035':
      return srid;
    default:
      return 'EPSG:4326';
  }
};
const checkValidCoordinates = (coordinates, emptyIsValid = false) => {
  if (emptyIsValid && coordinates === '') return true;
  if (coordinates === '') return false;
  if (!Array.isArray(coordinates)) {
    if (coordinates.toString().indexOf(',') === -1) return false;
  } else {
    if (isEmpty(coordinates)) return false;
  }
  let isValid = true;
  const splittedCoordinates = Array.isArray(coordinates) ? coordinates : coordinates.split(',');
  if (splittedCoordinates.length < 2) {
    isValid = false;
  } else {
    splittedCoordinates.forEach(coordinate => {
      if (isNil(coordinate) || coordinate.toString().trim() === '' || isNaN(coordinate.toString().trim()))
        isValid = false;
    });
  }
  return isValid;
};

const checkValidLine = (coordinates, emptyIsValid = false) => {
  if (coordinates.length < 2) return false;
  let isValid = true;
  coordinates.forEach(coordinate => {
    if (!checkValidCoordinates(coordinate, emptyIsValid)) {
      isValid = false;
    }
  });
  return isValid;
};

const isValidJSON = value => {
  if (isNil(value) || value.trim() === '' || value.indexOf('{') === -1) return false;
  try {
    JSON.parse(value);
  } catch (e) {
    return false;
  }
  return true;
};

const checkValidJSONCoordinates = json => {
  if (isValidJSON(json)) {
    const parsedJSON = JSON.parse(json);
    return checkValidCoordinates(parsedJSON.geometry.coordinates);
  } else {
    return false;
  }
};

const checkValidJSONMultipleCoordinates = (json, complexType = false) => {
  if (isValidJSON(json)) {
    debugger;
    const parsedJSON = JSON.parse(json);
    if (complexType) {
      return (
        parsedJSON.geometry.coordinates
          .map(coordinates => coordinates.map(coordinate => checkValidCoordinates(coordinate)))
          .filter(check => !check).length === 0
      );
    } else {
      return (
        parsedJSON.geometry.coordinates.map(coordinate => checkValidCoordinates(coordinate)).filter(check => !check)
          .length === 0
      );
    }
  } else {
    return false;
  }
};

const getFirstPointComplexGeometry = (json, geometryType) =>
  !isNil(json)
    ? first(
        ['POLYGON', 'MULTIPOLYGON'].includes(geometryType)
          ? first(JSON.parse(json).geometry.coordinates)
          : JSON.parse(json).geometry.coordinates
      )
    : [55.6811608, 12.5844761];

const getGeometryType = json =>
  !isNil(json) && isValidJSON(json) ? JSON.parse(json).geometry.type.toUpperCase() : 'POINT';

const getSrid = json => (!isNil(json) && json !== '' ? JSON.parse(json).properties.srid : 'EPSG:4326');

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
  const baseJson = `{"type": "Feature", "geometry": {"type":"Point","coordinates":[55.6811608,12.5844761]}, "properties": {"srid": "EPSG:4326"}}`;
  records.forEach(record =>
    record.dataRow.forEach(row => {
      if (row.fieldData.type === 'POINT') {
        const parsedJSON = JSON.parse(baseJson);
        const value = !isNil(row.fieldData[Object.keys(row.fieldData)[0]])
          ? row.fieldData[Object.keys(row.fieldData)[0]]
          : ',';
        const splittedValue = value.split(/-EPSG|- EPSG| - EPSG/);
        if (!checkValidCoordinates(splittedValue.length === 0 ? value : splittedValue[0])) {
          parsedJSON.geometry.coordinates = [''];
        } else {
          parsedJSON.geometry.coordinates =
            splittedValue.length === 0
              ? [value.replace(', ', ',').split(',')[0], value.replace(', ', ',').split(',')[1]]
              : [splittedValue[0].replace(', ', ',').split(',')[0], splittedValue[0].replace(', ', ',').split(',')[1]];
          parsedJSON.properties.srid = splittedValue.length === 0 ? '4326' : checkSRID(splittedValue[1]);
        }

        row.fieldData[Object.keys(row.fieldData)[0]] = JSON.stringify(parsedJSON);
      }
    })
  );
  return records;
};

const printCoordinates = (data, isGeoJson = true) => {
  if (isGeoJson) {
    if (!Array.isArray(data) && checkValidJSONCoordinates(data)) {
      let parsedJSON = data;
      if (typeof parsedJSON === 'string') {
        parsedJSON = JSON.parse(data);
      }
      return `{Latitude: ${parsedJSON.geometry.coordinates[0]}, Longitude: ${parsedJSON.geometry.coordinates[1]}}`;
    } else {
      if (Array.isArray(data)) {
        return `{Latitude: ${data[0]}, Longitude: ${data[1]}}`;
      } else {
        return `{Latitude: , Longitude: }`;
      }
    }
  } else {
    if (!isNil(data)) {
      const splittedCoordinate = Array.isArray(data) ? data : data.replace(', ', ',').split(',');
      return `{Latitude: ${isNil(splittedCoordinate[0]) ? '' : splittedCoordinate[0]}, Longitude: ${
        isNil(splittedCoordinate[1]) ? '' : splittedCoordinate[1]
      }}`;
    } else {
      return `{Latitude: , Longitude: }`;
    }
  }
};

export const MapUtils = {
  changeIncorrectCoordinates,
  checkValidCoordinates,
  checkValidJSONCoordinates,
  checkValidJSONMultipleCoordinates,
  checkValidLine,
  getFirstPointComplexGeometry,
  getGeometryType,
  getSrid,
  isValidJSON,
  latLngToLngLat,
  lngLatToLatLng,
  parseCoordinates,
  parseGeometryData,
  printCoordinates
};
