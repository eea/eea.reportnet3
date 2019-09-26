import React from 'react';

import styles from './SnapshotList.module.scss';

import { SnapshotItem } from './_components/SnapshotItem';

export function SnapshotList({ snapshotListData, onLoadSnapshotList, dataflowId, dataSetId }) {
  return (
    <div className={`${styles.listContainer}  ${styles.section}`}>
      <ul>
        {snapshotListData.map(item => (
          <div key={item.id}>
            <SnapshotItem
              itemData={item}
              onLoadSnapshotList={onLoadSnapshotList}
              dataflowId={dataflowId}
              dataSetId={dataSetId}
            />
            <hr />
          </div>
        ))}
      </ul>
    </div>
  );
}
