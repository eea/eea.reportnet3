import React from 'react';

import styles from './SnapshotList.module.scss';

import { SnapshotItem } from './_components/SnapshotItem';

export function SnapshotList({ snapshotListData, onLoadSnapshotList, dataFlowId, dataSetId }) {
  return (
    <div className={`${styles.listContainer}  ${styles.section}`}>
      <ul>
        {snapshotListData.map(item => (
          <>
            <SnapshotItem
              itemData={item}
              key={item.id}
              onLoadSnapshotList={onLoadSnapshotList}
              dataFlowId={dataFlowId}
              dataSetId={dataSetId}
            />
            <hr />
          </>
        ))}
      </ul>
    </div>
  );
}
