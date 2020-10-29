import { CreateDesigner } from './CreateDesigner';
import { DeleteDesigner } from './DeleteDesigner';
import { GetAllDesigner } from './GetAllDesigner';
import { RestoreDesigner } from './RestoreDesigner';

import { CreateReporter } from './CreateReporter';
import { DeleteReporter } from './DeleteReporter';
import { GetAllReporter } from './GetAllReporter';
import { RestoreReporter } from './RestoreReporter';

import { ReleaseDataflow } from './ReleaseDataflow';
import { snapshotRepository } from 'core/domain/model/Snapshot/SnapshotRepository';

export const SnapshotService = {
  allDesigner: GetAllDesigner({ snapshotRepository }),
  createByIdDesigner: CreateDesigner({ snapshotRepository }),
  deleteByIdDesigner: DeleteDesigner({ snapshotRepository }),
  restoreByIdDesigner: RestoreDesigner({ snapshotRepository }),

  allReporter: GetAllReporter({ snapshotRepository }),
  createByIdReporter: CreateReporter({ snapshotRepository }),
  deleteByIdReporter: DeleteReporter({ snapshotRepository }),
  restoreByIdReporter: RestoreReporter({ snapshotRepository }),
  
  releaseDataflow: ReleaseDataflow({ snapshotRepository })
};
