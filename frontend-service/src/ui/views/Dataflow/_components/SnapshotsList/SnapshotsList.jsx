import React from 'react';

import styles from './SnapshotsList.module.scss';

import { SnapshotItem } from './_components/SnapshotItem';

export const SnapshotsList = ({
  snapshotsListData,
  onLoadSnapshotList,
  setIsActiveReleaseSnapshotConfirmDialog,
  setSnapshotDataToRelease
}) => {
  return (
    <div className={`${styles.listContainer}  ${styles.section}`}>
      <ul>
        {snapshotsListData.map(item => (
          <div key={item.id}>
            <SnapshotItem
              itemData={item}
              onLoadSnapshotList={onLoadSnapshotList}
              setIsActiveReleaseSnapshotConfirmDialog={setIsActiveReleaseSnapshotConfirmDialog}
              setSnapshotDataToRelease={setSnapshotDataToRelease}
            />
          </div>
        ))}
      </ul>
    </div>
  );
};
