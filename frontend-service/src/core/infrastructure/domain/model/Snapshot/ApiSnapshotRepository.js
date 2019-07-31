import { api } from 'core/infrastructure/api';
import { Snapshot } from 'core/domain/model/Snapshot/Snapshot';

const all = async () => {
  const snapshotsDTO = await api.snapshots();
  return snapshotsDTO.map(snapshotDTO => new Snapshot(snapshotDTO.creationDate, snapshotDTO.description));
};

export const ApiSnapshotRepository = {
  all
};
