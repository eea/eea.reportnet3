import { CreateSnapshot } from './CreateSnapshot';
import { DeleteSnapshot } from './DeleteSnapshot';
import { GetAllSnapshots } from './GetAllSnapshots';
import { RestoreSnapshot } from './RestoreSnapshot';
import { snapshotRepository } from 'core/domain/model/Snapshot/SnapshotRepository';

export const SnapshotService = {
  all: GetAllSnapshots({ snapshotRepository }),
  createById: CreateSnapshot({ snapshotRepository }),
  deleteById: DeleteSnapshot({ snapshotRepository }),
  restoreById: RestoreSnapshot({ snapshotRepository })
};
