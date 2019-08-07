import { dataSetRepository } from 'core/domain/model/DataSet/DataSetRepository';
import { GetDataSetErrors } from './GetDataSetErrors';
import { GetDataSetStatistics } from './GetDataSetStatistics';
import { GetErrorPosition } from './GetErrorPosition';

export const DataSetService = {
  errorsById: GetDataSetErrors({ dataSetRepository }),
  errorPositionByObjectId: GetErrorPosition({ dataSetRepository }),
  errorStatisticsById: GetDataSetStatistics({ dataSetRepository })
};
