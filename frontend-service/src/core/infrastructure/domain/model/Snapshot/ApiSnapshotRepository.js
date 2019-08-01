import { api } from 'core/infrastructure/api';
import { Snapshot } from 'core/domain/model/Snapshot/Snapshot';

const all = async url => {
  const snapshotsDTO = await api.snapshots(url);
  return snapshotsDTO.map(snapshotDTO => new Snapshot(snapshotDTO.creationDate, snapshotDTO.description));
};

export const ApiSnapshotRepository = {
  all
};
