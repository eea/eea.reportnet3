import isNil from 'lodash/isNil';
import isUndefined from 'lodash/isUndefined';

export const CoreUtils = (() => {
  const UtilsAPI = {
    getDashboardLevelErrorByDataset: datasetDTO =>
      datasetDTO.map(datasetTableDTO => UtilsAPI.getDashboardLevelErrorByTable(datasetTableDTO)),

    getDashboardLevelErrorByTable: datasetTableDTO => {
      const levelErrors = [];
      datasetTableDTO.tables.forEach(datasetTableDTO => {
        const corrects =
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
      switch (levelError) {
        case 'CORRECT':
          return 0;
        case 'INFO':
          return 1;
        case 'WARNING':
          return 2;
        case 'ERROR':
          return 3;
        case 'BLOCKER':
          return 4;
        case '':
          return 99;
        default:
          return null;
      }
    },

    getPercentage: valArr => {
      const total = valArr.reduce((arr1, arr2) => arr1.map((v, i) => v + arr2[i]));
      return valArr.map(val => val.map((v, i) => ((v / total[i]) * 100).toFixed(2)));
    },

    getPercentageOfValue: (val, total) => (total === 0 ? '0.00' : ((val / total) * 100).toFixed(2)),

    isDuplicatedInObject: (array, property) => {
      let isDuplicated = false;
      const testObject = {};

      array.forEach(item => {
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
      const valuesWithValidations = CoreUtils.transposeMatrix(tableStatisticValues);
      const tableStatisticValuesWithSomeError = [];

      valuesWithValidations.forEach(item => {
        if (!isNil(item) && !item.every(value => value === 0)) {
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
