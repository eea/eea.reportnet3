import { dataSetRepository } from 'core/domain/model/DataSet/DataSetRepository';
import { GetDataSetErrors } from './GetDataSetErrors';
import { GetErrorPosition } from './GetErrorPosition';
import { GetDataSetStatistics } from './GetDataSetStatistics';

export const DataSetService = {
  errorsById: GetDataSetErrors({ dataSetRepository }),
  errorPositionByObjectId: GetErrorPosition({ dataSetRepository })
};
