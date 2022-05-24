import first from 'lodash/first';
import flattenDeep from 'lodash/flattenDeep';
import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';

import { config } from 'conf';

import proj4 from 'proj4';

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
  if (emptyIsValid && coordinates === '') {
    return true;
  }

  if (coordinates === '') {
    return false;
  }

  if (isNil(coordinates)) {
    return false;
  }

  if (!Array.isArray(coordinates)) {
    if (coordinates.toString().indexOf(',') === -1) {
      return false;
    }
  } else {
    if (isEmpty(coordinates)) {
      return false;
    }
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
  if (isNil(value) || value.trim() === '' || value.indexOf('{') === -1) {
    return false;
  }

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

const getGeometryType = json => (!isNil(json) && isValidJSON(json) ? JSON.parse(json).geometry.type.toUpperCase() : '');

const getSrid = json => (!isNil(json) && json !== '' ? JSON.parse(json).properties.srid : 'EPSG:4326');

const hasValidCRS = (fieldValue, crs) => {
  if (fieldValue === '') {
    return true;
  }

  const parsedGeoJsonData = JSON.parse(fieldValue);
  return crs.some(crsItem => crsItem.value === parsedGeoJsonData.properties.srid);
};

const inBounds = ({ coord, coordType, checkProjected = false }) => {
  const parsedCoord = parseFloat(coord) || 0;
  if (checkProjected) {
    if (coordType === 'latitude') {
      return (
        parsedCoord >= config.GEOGRAPHICAL_LAT_COORD_3035.min && parsedCoord <= config.GEOGRAPHICAL_LAT_COORD_3035.max
      );
    } else {
      return (
        parsedCoord >= config.GEOGRAPHICAL_LONG_COORD_3035.min && parsedCoord <= config.GEOGRAPHICAL_LONG_COORD_3035.max
      );
    }
  } else {
    if (coordType === 'latitude') {
      return parsedCoord >= config.GEOGRAPHICAL_LAT_COORD.min && parsedCoord <= config.GEOGRAPHICAL_LAT_COORD.max;
    } else {
      return parsedCoord >= config.GEOGRAPHICAL_LONG_COORD.min && parsedCoord <= config.GEOGRAPHICAL_LONG_COORD.max;
    }
  }
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
              ? [Number(TextUtils.splitByChar(value)[0]), Number(TextUtils.splitByChar(value)[1])]
              : [
                  Number(TextUtils.splitByChar(splittedValue[0])[0]),
                  Number(TextUtils.splitByChar(splittedValue[0])[1])
                ];
          parsedJSON.properties.srid = splittedValue.length === 0 ? '4326' : checkSRID(splittedValue[1]);
        }

        row.fieldData[Object.keys(row.fieldData)[0]] = JSON.stringify(parsedJSON);
      }
    })
  );
  return records;
};

const printCoordinates = ({
  data,
  isGeoJson = true,
  geometryType,
  firstCoordinateText = 'Latitude',
  secondCoordinateText = 'Longitude'
}) => {
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
      return `{${firstCoordinateText}: ${parsedJSON.geometry.coordinates[0]}, ${secondCoordinateText}: ${parsedJSON.geometry.coordinates[1]}}`;
    } else {
      if (Array.isArray(data)) {
        return `{${firstCoordinateText}: ${data[0]}, ${secondCoordinateText}: ${data[1]}}`;
      } else {
        return `{${firstCoordinateText}: , ${secondCoordinateText}: }`;
      }
    }
  } else {
    if (!isNil(data)) {
      const splittedCoordinate = Array.isArray(data) ? data : TextUtils.splitByChar(data);
      return `{${firstCoordinateText}: ${
        isNil(splittedCoordinate[0]) ? '' : splittedCoordinate[0]
      }, ${secondCoordinateText}: ${isNil(splittedCoordinate[1]) ? '' : splittedCoordinate[1]}}`;
    } else {
      return `{${firstCoordinateText}: , ${secondCoordinateText}: }`;
    }
  }
};

const projectCoordinates = ({ coordinates, currentCRS, newCRS }) => {
  if (checkValidCoordinates(coordinates)) {
    if (newCRS === 'EPSG:3035') {
      return proj4(proj4(currentCRS.value), proj4(newCRS), [coordinates[1], coordinates[0]]);
    } else {
      const projectedCoordinates = proj4(proj4(currentCRS.value), proj4(newCRS), coordinates);
      if (currentCRS.value === 'EPSG:3035') {
        return [projectedCoordinates[1], projectedCoordinates[0]];
      } else {
        return projectedCoordinates;
      }
    }
  } else {
    return coordinates;
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
  hasValidCRS,
  inBounds,
  isValidJSON,
  latLngToLngLat,
  lngLatToLatLng,
  parseCoordinates,
  parseGeometryData,
  printCoordinates,
  projectCoordinates
};
