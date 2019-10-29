import React from 'react';

import styles from './SnapshotsList.module.scss';

import { SnapshotItem } from './_components/SnapshotItem';

export function SnapshotsList({ snapshotsListData, onLoadSnapshotList, dataflowId, datasetId }) {
  return (
    <div className={`${styles.listContainer}  ${styles.section}`}>
      <ul>
        {snapshotsListData.map(item => (
          <div key={item.id}>
            <SnapshotItem
              itemData={item}
              onLoadSnapshotList={onLoadSnapshotList}
              dataflowId={dataflowId}
              datasetId={datasetId}
            />
            <hr />
          </div>
        ))}
      </ul>
    </div>
  );
}
