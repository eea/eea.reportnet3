import snakeCase from 'lodash/snakeCase';

export const ErrorUtils = {
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

  parseErrorType: errorType => {
    return `${snakeCase(errorType).toUpperCase()}_ERROR`;
  }
};
