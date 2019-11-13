import { CreateDesigner } from './CreateDesigner';
import { DeleteDesigner } from './DeleteDesigner';
import { GetAllDesigner } from './GetAllDesigner';
import { ReleaseDesigner } from './ReleaseDesigner';
import { RestoreDesigner } from './RestoreDesigner';

import { CreateReporter } from './CreateReporter';
import { DeleteReporter } from './DeleteReporter';
import { GetAllReporter } from './GetAllReporter';
import { ReleaseReporter } from './ReleaseReporter';
import { RestoreReporter } from './RestoreReporter';
import { snapshotRepository } from 'core/domain/model/Snapshot/SnapshotRepository';

export const SnapshotService = {
  allDesigner: GetAllDesigner({ snapshotRepository }),
  createByIdDesigner: CreateDesigner({ snapshotRepository }),
  deleteByIdDesigner: DeleteDesigner({ snapshotRepository }),
  releaseByIdDesigner: ReleaseDesigner({ snapshotRepository }),
  restoreByIdDesigner: RestoreDesigner({ snapshotRepository }),

  allReporter: GetAllReporter({ snapshotRepository }),
  createByIdReporter: CreateReporter({ snapshotRepository }),
  deleteByIdReporter: DeleteReporter({ snapshotRepository }),
  releaseByIdReporter: ReleaseReporter({ snapshotRepository }),
  restoreByIdReporter: RestoreReporter({ snapshotRepository })
};
