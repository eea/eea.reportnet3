import { dataSetRepository } from 'core/domain/model/DataSet/DataSetRepository';
import { DeleteData } from './DeleteData';
import { GetDataSetErrors } from './GetDataSetErrors';
import { GetDataSetSchema } from './GetDataSetSchema';
import { GetDataSetStatistics } from './GetDataSetStatistics';
import { GetErrorPosition } from './GetErrorPosition';
import { ValidateData } from './ValidateData';

export const DataSetService = {
  dataSetSchemaById: GetDataSetSchema({ dataSetRepository }),
  deleteDataById: DeleteData({ dataSetRepository }),
  errorsById: GetDataSetErrors({ dataSetRepository }),
  errorPositionByObjectId: GetErrorPosition({ dataSetRepository }),
  errorStatisticsById: GetDataSetStatistics({ dataSetRepository }),
  validateDataById: ValidateData({ dataSetRepository })
};
