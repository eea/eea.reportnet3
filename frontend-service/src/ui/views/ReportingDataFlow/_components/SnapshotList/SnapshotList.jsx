import React from 'react';

import styles from './SnapshotList.module.scss';

import { SnapshotItem } from './_components/SnapshotItem';

export function SnapshotList({ snapshotListData, onLoadSnapshotList, dataflowId, datasetId }) {
  return (
    <div className={`${styles.listContainer}  ${styles.section}`}>
      <ul>
        {snapshotListData.map(item => (
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
