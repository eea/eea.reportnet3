import { euDatasetRepository } from 'core/domain/model/EuDataset/EuDatasetRepository';

import { CopyDataCollection } from './CopyDataCollection';
import { ExportEuDataset } from './ExportEuDataset';

export const EuDatasetService = {
  copyDataCollection: CopyDataCollection({ euDatasetRepository }),
  exportEuDataset: ExportEuDataset({ euDatasetRepository })
};
