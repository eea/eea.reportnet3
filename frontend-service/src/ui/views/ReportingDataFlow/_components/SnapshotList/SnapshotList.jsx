import React from 'react';

import styles from './SnapshotList.module.scss';

import { SnapshotItem } from './_components/SnapshotItem';

export function SnapshotList({ snapshotListData, onLoadSnapshotList, dataFlowId, dataSetId }) {
  return (
    <div className={`${styles.listContainer}  ${styles.section}`}>
      <ul>
        {snapshotListData.map(item => (
          <div key={item.id}>
            <SnapshotItem
              itemData={item}
              onLoadSnapshotList={onLoadSnapshotList}
              dataFlowId={dataFlowId}
              dataSetId={dataSetId}
            />
            <hr />
          </div>
        ))}
      </ul>
    </div>
  );
}
