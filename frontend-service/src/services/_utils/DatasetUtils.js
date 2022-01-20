import dayjs from 'dayjs';

import cloneDeep from 'lodash/cloneDeep';
import isNil from 'lodash/isNil';

import { Dataset } from 'entities/Dataset';

import { CoreUtils } from 'repositories/_utils/CoreUtils';

const sortDatasetTypeByName = (a, b) => {
  const datasetName_A = a.datasetSchemaName;
  const datasetName_B = b.datasetSchemaName;
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
    status: datasetDTO.status,
    updatable: datasetDTO.updatable
  });

const getAllLevelErrorsFromRuleValidations = rulesDTO =>
  CoreUtils.orderLevelErrors([
    ...new Set(rulesDTO.rules.map(rule => rule.thenCondition).map(condition => condition[1]))
  ]);

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

const tableStatisticValuesWithErrors = tableStatisticValues => {
  const valuesWithValidations = CoreUtils.transposeMatrix(tableStatisticValues);
  const tableStatisticValuesWithSomeError = [];

  valuesWithValidations.forEach(item => {
    if (!isNil(item) && !item.every(value => value === 0)) {
      tableStatisticValuesWithSomeError.push(item);
    }
  });

  return tableStatisticValuesWithSomeError;
};

const parseValue = ({ type, value, splitSRID = false }) => {
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
      if (parsedValue.properties.srid !== 'EPSG:3035' && parsedValue.properties.srid !== '3035') {
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
    }

    if (!splitSRID) {
      inmValue.properties.srid = `EPSG:${parsedValue.properties.srid}`;
    } else {
      inmValue.properties.srid = parsedValue.properties.srid.split(':')[1];
    }

    return JSON.stringify(inmValue);
  }
  return value;
};

const getValidExtensions = ({ isTooltip = false, validExtensions = '' }) =>
  validExtensions
    ?.split(/,\s*/)
    .map(ext => (isTooltip ? ` .${ext}` : `.${ext}`))
    .join(',');

export const DatasetUtils = {
  getAllLevelErrorsFromRuleValidations,
  getValidExtensions,
  parseDatasetListDTO,
  parseValue,
  sortDatasetTypeByName,
  tableStatisticValuesWithErrors
};
