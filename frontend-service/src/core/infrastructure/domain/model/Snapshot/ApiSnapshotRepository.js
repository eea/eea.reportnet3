import { api } from 'core/infrastructure/api';
import { Snapshot } from 'core/domain/model/Snapshot/Snapshot';

const all = async url => {
  const snapshotsDTO = await api.snapshots(url);
  return snapshotsDTO.map(snapshotDTO => new Snapshot(snapshotDTO.id, snapshotDTO.created_at, snapshotDTO.description));
};

export const ApiSnapshotRepository = {
  all
};
