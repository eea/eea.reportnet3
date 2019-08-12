import { Create } from './Create';
import { Delete } from './Delete';
import { GetAll } from './GetAll';
import { Restore } from './Restore';
import { snapshotRepository } from 'core/domain/model/Snapshot/SnapshotRepository';

export const SnapshotService = {
  all: GetAll({ snapshotRepository }),
  createById: Create({ snapshotRepository }),
  deleteById: Delete({ snapshotRepository }),
  restoreById: Restore({ snapshotRepository })
};
