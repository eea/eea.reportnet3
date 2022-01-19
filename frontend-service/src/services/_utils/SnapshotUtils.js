import { Snapshot } from 'entities/Snapshot';

const parseSnapshotListDTO = snapshotsDTO =>
  snapshotsDTO?.map(
    snapshotDTO =>
      new Snapshot({
        creationDate: snapshotDTO.creationDate,
        description: snapshotDTO.description,
        id: snapshotDTO.id,
        isAutomatic: snapshotDTO.automatic,
        isReleased: snapshotDTO.release
      })
  );

export const SnapshotUtils = { parseSnapshotListDTO };
