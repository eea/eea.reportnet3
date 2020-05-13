import { getUrl } from 'core/infrastructure/CoreUtils';
import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { UniqueConstraintsConfig } from 'conf/domain/model/UniqueConstraints';
import { userStorage } from 'core/domain/model/User/UserStorage';

const data = {
  status: 200,
  data: {
    datasetSchemaId: 777,
    list: [
      {
        automatic: true,
        constraintsId: '5ebbb0dee2ad5400016d5a70',
        constraintsName: 'First Constraints',
        description: 'Checks if the unique exists',
        enabled: true,
        fieldIds: [123, 234, 345, 457],
        fieldNames: ['First field', 'Second field', 'Third field', 'Fourth field'],
        referenceId: '5ebbb0de07571e000151d063',
        shortCode: 'FC1',
        tableId: 789,
        tableName: 'My beauty table',
        thenCondition: ['This is the condition', 'ERROR']
      },
      {
        automatic: false,
        constraintsId: '07a5d6100045da2eed0bbbe5',
        constraintsName: 'Second Constraints',
        description: 'Checks if the constraints exists',
        enabled: false,
        fieldIds: [321, 432, 534, 754],
        fieldNames: ['Fifth field', 'Sixth field', 'Seventh field', 'Eighth field'],
        referenceId: '360d151000e17570ed0bbbe5',
        shortCode: 'FC2',
        tableId: 987,
        tableName: 'My ugly table',
        thenCondition: ['This is another condition', 'WARNING']
      }
    ]
  }
};

export const apiUniqueConstraints = {
  all: async () => {
    const tokens = userStorage.get();
    const response = await data;
    return response.data;
  }
};
