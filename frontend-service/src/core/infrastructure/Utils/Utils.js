export const Utils = (() => {
  const UtilsAPI = {
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
        default:
          levelErrorIndex = null;
      }
      return levelErrorIndex;
    },

    getPercentage: valArr => {
      let total = valArr.reduce((arr1, arr2) => arr1.map((v, i) => v + arr2[i]));
      return valArr.map(val => val.map((v, i) => ((v / total[i]) * 100).toFixed(2)));
    },

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

    transposeMatrix: matrix => {
      return Object.keys(matrix[0]).map(c => matrix.map(r => r[c]));
    }
  };
  return UtilsAPI;
})();
