import { isUndefined } from 'lodash';

export const CoreUtils = (() => {
  const UtilsAPI = {
    getDashboardLevelErrorByDataset: datasetDTO => {
      let levelErrors = [];
      datasetDTO.forEach(datasetTableDTO => {
        levelErrors.push(UtilsAPI.getDashboardLevelErrorByTable(datasetTableDTO));
      });
      return levelErrors;
    },

    getDashboardLevelErrorByTable: datasetTableDTO => {
      let levelErrors = [];
      datasetTableDTO.tables.forEach(datasetTableDTO => {
        let corrects =
          datasetTableDTO.totalRecords -
          (datasetTableDTO.totalRecordsWithBlockers +
            datasetTableDTO.totalRecordsWithErrors +
            datasetTableDTO.totalRecordsWithWarnings +
            datasetTableDTO.totalRecordsWithInfos);
        if (corrects > 0) {
          levelErrors.push('CORRECT');
        }
        if (datasetTableDTO.totalRecordsWithInfos > 0) {
          levelErrors.push('INFO');
        }
        if (datasetTableDTO.totalRecordsWithWarnings > 0) {
          levelErrors.push('WARNING');
        }
        if (datasetTableDTO.totalRecordsWithErrors > 0) {
          levelErrors.push('ERROR');
        }
        if (datasetTableDTO.totalRecordsWithBlockers > 0) {
          levelErrors.push('BLOCKER');
        }
      });
      return [...new Set(levelErrors)];
    },

    getLevelErrorPriorityByLevelError: levelError => {
      let levelErrorIndex = 0;
      switch (levelError) {
        case 'CORRECT':
          levelErrorIndex = 0;
          break;
        case 'INFO':
          levelErrorIndex = 1;
          break;
        case 'WARNING':
          levelErrorIndex = 2;
          break;
        case 'ERROR':
          levelErrorIndex = 3;
          break;
        case 'BLOCKER':
          levelErrorIndex = 4;
          break;
        case '':
          levelErrorIndex = 99;
          break;
        default:
          levelErrorIndex = null;
      }
      return levelErrorIndex;
    },

    getPercentage: valArr => {
      let total = valArr.reduce((arr1, arr2) => arr1.map((v, i) => v + arr2[i]));
      return valArr.map(val => val.map((v, i) => ((v / total[i]) * 100).toFixed(2)));
    },

    isDuplicatedInObject: (array, property) => {
      let isDuplicated = false,
        testObject = {};

      array.map(item => {
        const itemPropertyName = item[property];
        if (itemPropertyName in testObject) {
          testObject[itemPropertyName].duplicatedRoles = true;
          item.duplicatedRoles = true;
          isDuplicated = true;
        } else {
          testObject[itemPropertyName] = item;
          delete item.duplicatedRoles;
        }
      });
      return isDuplicated;
    },

    onGroupBy: key => array =>
      array.reduce((objectsByKeyValue, obj) => {
        const value = obj[key];
        objectsByKeyValue[value] = (objectsByKeyValue[value] || []).concat(obj);
        return objectsByKeyValue;
      }, {}),

    orderLevelErrors: levelErrors => {
      const levelErrorsWithPriority = [
        { id: 'CORRECT', index: 0 },
        { id: 'INFO', index: 1 },
        { id: 'WARNING', index: 2 },
        { id: 'ERROR', index: 3 },
        { id: 'BLOCKER', index: 4 }
      ];

      return levelErrors
        .map(error => levelErrorsWithPriority.filter(e => error === e.id))
        .flat()
        .sort((a, b) => a.index - b.index)
        .map(orderedError => orderedError.id);
    },

    tableStatisticValuesWithErrors: tableStatisticValues => {
      let tableStatisticValuesWithSomeError = [];
      let valuesWithValidations = CoreUtils.transposeMatrix(tableStatisticValues).map(error => {
        return error.map(subError => {
          return subError;
        });
      });
      valuesWithValidations.map(item => {
        if (item != null && item != undefined && !item.every(value => value === 0)) {
          tableStatisticValuesWithSomeError.push(item);
        }
      });
      return tableStatisticValuesWithSomeError;
    },

    transposeMatrix: matrix => {
      if (!isUndefined(matrix[0])) {
        return Object.keys(matrix[0]).map(c => matrix.map(r => r[c]));
      }
    }
  };
  return UtilsAPI;
})();
