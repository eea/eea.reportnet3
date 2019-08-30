import { Create } from './Create';
import { Delete } from './Delete';
import { GetAll } from './GetAll';
import { Release } from './Release';
import { Restore } from './Restore';
import { snapshotRepository } from 'core/domain/model/Snapshot/SnapshotRepository';

export const SnapshotService = {
  all: GetAll({ snapshotRepository }),
  createById: Create({ snapshotRepository }),
  deleteById: Delete({ snapshotRepository }),
  releaseById: Release({ snapshotRepository }),
  restoreById: Restore({ snapshotRepository })
};
