import dayjs from 'dayjs';

import cloneDeep from 'lodash/cloneDeep';
import isNil from 'lodash/isNil';

import { Dataset } from 'entities/Dataset';

import { CoreUtils } from 'repositories/_utils/CoreUtils';

const sortDatasetTypeByName = (a, b) => {
  let datasetName_A = a.datasetSchemaName;
  let datasetName_B = b.datasetSchemaName;
  return datasetName_A < datasetName_B ? -1 : datasetName_A > datasetName_B ? 1 : 0;
};

const parseDatasetListDTO = datasetsDTO => datasetsDTO?.map(datasetDTO => parseDatasetDTO(datasetDTO));

const parseDatasetDTO = datasetDTO =>
  new Dataset({
    availableInPublic: datasetDTO.availableInPublic,
    dataProviderId: datasetDTO.dataProviderId,
    datasetId: datasetDTO.id,
    datasetSchemaId: datasetDTO.datasetSchema,
    datasetSchemaName: datasetDTO.dataSetName,
    isReleased: datasetDTO.isReleased,
    isReleasing: datasetDTO.releasing,
    name: datasetDTO.nameDatasetSchema,
    publicFileName: datasetDTO.publicFileName,
    referenceDataset: datasetDTO.referenceDataset,
    releaseDate: datasetDTO.dateReleased > 0 ? dayjs(datasetDTO.dateReleased).format('YYYY-MM-DD HH:mm') : '-',
    restrictFromPublic: datasetDTO.restrictFromPublic,
    technicalAcceptanceStatus: datasetDTO.status,
    updatable: datasetDTO.updatable
  });

const getAllLevelErrorsFromRuleValidations = rulesDTO =>
  CoreUtils.orderLevelErrors([
    ...new Set(rulesDTO.rules.map(rule => rule.thenCondition).map(condition => condition[1]))
  ]);

const isValidJSON = value => {
  if (isNil(value) || value.trim() === '' || value.indexOf('{') === -1) return false;
  try {
    JSON.parse(value);
  } catch (error) {
    return false;
  }
  return true;
};

const tableStatisticValuesWithErrors = tableStatisticValues => {
  let tableStatisticValuesWithSomeError = [];
  let valuesWithValidations = CoreUtils.transposeMatrix(tableStatisticValues).map(error => {
    return error.map(subError => {
      return subError;
    });
  });
  valuesWithValidations.forEach(item => {
    if (!isNil(item) && !item.every(value => value === 0)) {
      tableStatisticValuesWithSomeError.push(item);
    }
  });
  return tableStatisticValuesWithSomeError;
};

const parseValue = (type, value, feToBe = false) => {
  if (
    ['POINT', 'LINESTRING', 'POLYGON', 'MULTILINESTRING', 'MULTIPOLYGON', 'MULTIPOINT'].includes(type) &&
    value !== '' &&
    !isNil(value)
  ) {
    if (!isValidJSON(value)) {
      return '';
    }
    const inmValue = JSON.parse(cloneDeep(value));
    const parsedValue = JSON.parse(value);

    if (parsedValue.geometry.type.toUpperCase() !== type) {
      if (type.toUpperCase() === 'POINT') {
        return '';
      }
      inmValue.geometry.type = type;
      inmValue.geometry.coordinates = [];
    } else {
      switch (type.toUpperCase()) {
        case 'POINT':
          inmValue.geometry.coordinates = [parsedValue.geometry.coordinates[1], parsedValue.geometry.coordinates[0]];
          break;
        case 'MULTIPOINT':
        case 'LINESTRING':
          inmValue.geometry.coordinates = parsedValue.geometry.coordinates.map(coordinate =>
            !isNil(coordinate) ? [coordinate[1], coordinate[0]] : []
          );
          break;
        case 'POLYGON':
        case 'MULTILINESTRING':
          inmValue.geometry.coordinates = parsedValue.geometry.coordinates.map(coordinate => {
            if (Array.isArray(coordinate)) {
              return coordinate.map(innerCoordinate =>
                !isNil(innerCoordinate) ? [innerCoordinate[1], innerCoordinate[0]] : []
              );
            } else {
              return [];
            }
          });
          break;
        case 'MULTIPOLYGON':
          inmValue.geometry.coordinates = parsedValue.geometry.coordinates.map(polygon => {
            if (Array.isArray(polygon)) {
              return polygon.map(coordinate => {
                if (Array.isArray(coordinate)) {
                  return coordinate.map(innerCoordinate =>
                    !isNil(innerCoordinate) ? [innerCoordinate[1], innerCoordinate[0]] : []
                  );
                } else {
                  return [];
                }
              });
            } else {
              return [];
            }
          });
          break;
        default:
          break;
      }
    }

    if (!feToBe) {
      inmValue.properties.srid = `EPSG:${parsedValue.properties.srid}`;
    } else {
      inmValue.properties.srid = parsedValue.properties.srid.split(':')[1];
    }

    return JSON.stringify(inmValue);
  }
  return value;
};

// const getPercentage = valArr => {
//   let total = valArr.reduce((arr1, arr2) => arr1.map((v, i) => v + arr2[i]));
//   return valArr.map(val => val.map((v, i) => ((v / total[i]) * 100).toFixed(2)));
// };

// const transposeMatrix = matrix => {
//   return Object.keys(matrix[0]).map(c => matrix.map(r => r[c]));
// };

export const DatasetUtils = {
  getAllLevelErrorsFromRuleValidations,
  parseDatasetListDTO,
  parseValue,
  sortDatasetTypeByName,
  tableStatisticValuesWithErrors
};
