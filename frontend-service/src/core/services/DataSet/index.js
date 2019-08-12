import { dataSetRepository } from 'core/domain/model/DataSet/DataSetRepository';
import { DeleteData } from './DeleteData';
import { GetErrors } from './GetErrors';
import { GetSchema } from './GetSchema';
import { GetStatistics } from './GetStatistics';
import { GetErrorPosition } from './GetErrorPosition';
import { ValidateData } from './ValidateData';

export const DataSetService = {
  dataSetSchemaById: GetSchema({ dataSetRepository }),
  deleteDataById: DeleteData({ dataSetRepository }),
  errorsById: GetErrors({ dataSetRepository }),
  errorPositionByObjectId: GetErrorPosition({ dataSetRepository }),
  errorStatisticsById: GetStatistics({ dataSetRepository }),
  validateDataById: ValidateData({ dataSetRepository })
};
