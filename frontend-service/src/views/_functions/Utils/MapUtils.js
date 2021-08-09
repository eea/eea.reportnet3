import first from 'lodash/first';
import flattenDeep from 'lodash/flattenDeep';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { TextUtils } from 'repositories/_utils/TextUtils';

const changeIncorrectCoordinates = record => {
  const baseJson = `{"type": "Feature", "geometry": {"type":"Point","coordinates":[]}, "properties": {"srid": "EPSG:4326"}}`;
  record.dataRow.forEach(row => {
    if (row.fieldData.type === 'POINT') {
      const parsedJSON = JSON.parse(
        !isNil(Object.values(row.fieldData)[0]) && !isEmpty(Object.values(row.fieldData)[0])
          ? Object.values(row.fieldData)[0]
          : baseJson
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
  if (isNil(coordinates)) return false;
  if (!Array.isArray(coordinates)) {
    if (coordinates.toString().indexOf(',') === -1) return false;
  } else {
    if (isEmpty(coordinates)) return false;
  }
  let isValid = true;
  const splittedCoordinates = Array.isArray(coordinates) ? coordinates : TextUtils.splitByChar(coordinates);
  if (splittedCoordinates.length < 2) {
    isValid = false;
  } else {
    splittedCoordinates.forEach(coordinate => {
      if (isNil(coordinate) || coordinate.toString().trim() === '' || isNaN(coordinate.toString().trim())) {
        isValid = false;
      }
    });
  }
  return isValid;
};

const isValidJSON = value => {
  if (isNil(value) || value.trim() === '' || value.indexOf('{') === -1) return false;
  try {
    JSON.parse(value);
  } catch (error) {
    return false;
  }
  return true;
};

const checkValidJSONCoordinates = json => {
  if (isValidJSON(json)) {
    const parsedJSON = JSON.parse(json);
    if (!isEmpty(parsedJSON.geometry.coordinates)) {
      return checkValidCoordinates(parsedJSON.geometry.coordinates);
    } else {
      return false;
    }
  } else {
    return false;
  }
};

const checkValidJSONMultipleCoordinates = json => {
  if (isValidJSON(json)) {
    const parsedJSON = JSON.parse(json);
    if (!isEmpty(parsedJSON.geometry.coordinates)) {
      switch (parsedJSON.geometry.type.toUpperCase()) {
        case 'LINESTRING':
        case 'MULTIPOINT':
          return (
            flattenDeep(parsedJSON.geometry.coordinates.map(coordinate => checkValidCoordinates(coordinate))).filter(
              check => !check
            ).length === 0
          );
        case 'MULTILINESTRING':
        case 'POLYGON':
          return (
            flattenDeep(
              parsedJSON.geometry.coordinates.map(
                ring => !isNil(ring) && ring.map(coordinate => checkValidCoordinates(coordinate))
              )
            ).filter(check => !check).length === 0
          );
        case 'MULTIPOLYGON':
          return (
            flattenDeep(
              parsedJSON.geometry.coordinates.map(polygon =>
                polygon.map(ring => !isNil(ring) && ring.map(coordinate => checkValidCoordinates(coordinate)))
              )
            ).filter(check => !check).length === 0
          );
        default:
          break;
      }
    } else {
      return false;
    }
  } else {
    return false;
  }
};

const getFirstPointComplexGeometry = (json, geometryType) =>
  !isNil(json)
    ? first(
        ['POLYGON', 'MULTILINESTRING'].includes(geometryType)
          ? first(JSON.parse(json).geometry.coordinates)
          : ['MULTIPOLYGON'].includes(geometryType)
          ? first(first(JSON.parse(json).geometry.coordinates))
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
              ? [TextUtils.splitByChar(value)[0], TextUtils.splitByChar(value)[1]]
              : [TextUtils.splitByChar(splittedValue[0])[0], TextUtils.splitByChar(splittedValue[0])[1]];
          parsedJSON.properties.srid = splittedValue.length === 0 ? '4326' : checkSRID(splittedValue[1]);
        }

        row.fieldData[Object.keys(row.fieldData)[0]] = JSON.stringify(parsedJSON);
      }
    })
  );
  return records;
};

const printCoordinates = (data, isGeoJson = true, geometryType) => {
  if (isGeoJson) {
    let parsedJSON = data;
    if (typeof parsedJSON === 'string') {
      parsedJSON = JSON.parse(data);
    }

    switch (geometryType.toUpperCase()) {
      case 'MULTIPOINT':
      case 'LINESTRING':
      case 'POLYGON':
      case 'MULTILINESTRING':
      case 'MULTIPOLYGON':
        return JSON.stringify(parsedJSON.geometry.coordinates);
      default:
        break;
    }

    if (!Array.isArray(data) && checkValidJSONCoordinates(data)) {
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
      const splittedCoordinate = Array.isArray(data) ? data : TextUtils.splitByChar(data);
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
