import { GetAllSnapshots } from './GetAllSnapshots';
import { snapshotRepository } from 'core/domain/model/Snapshot/SnapshotRepository';

export const SnapshotService = {
  all: GetAllSnapshots({ snapshotRepository })
};
