import { CreateDesigner } from './CreateDesigner';
import { CreateReporter } from './CreateReporter';
import { DeleteDesigner } from './DeleteDesigner';
import { DeleteReporter } from './DeleteReporter';
import { GetAllDesigner } from './GetAllDesigner';
import { GetAllReporter } from './GetAllReporter';
import { ReleaseDataflow } from './ReleaseDataflow';
import { RestoreDesigner } from './RestoreDesigner';
import { RestoreReporter } from './RestoreReporter';

import { snapshotRepository } from 'entities/Snapshot/SnapshotRepository';

export const SnapshotService = {
  allDesigner: GetAllDesigner({ snapshotRepository }),
  allReporter: GetAllReporter({ snapshotRepository }),
  createByIdDesigner: CreateDesigner({ snapshotRepository }),
  createByIdReporter: CreateReporter({ snapshotRepository }),
  deleteByIdDesigner: DeleteDesigner({ snapshotRepository }),
  deleteByIdReporter: DeleteReporter({ snapshotRepository }),
  releaseDataflow: ReleaseDataflow({ snapshotRepository }),
  restoreByIdDesigner: RestoreDesigner({ snapshotRepository }),
  restoreByIdReporter: RestoreReporter({ snapshotRepository })
};
