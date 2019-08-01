import React from 'react';

import styles from './SnapshotList.module.scss';

import { SnapshotItem } from './_components/SnapshotItem';

export function SnapshotList({ snapshotListData }) {
  return (
    <div className={`${styles.listContainer}  ${styles.section}`}>
      <ul>
        {/* TODO ADD Scrolling  */}
        {snapshotListData.map(item => (
          <SnapshotItem itemData={item} key={item.id} />
        ))}
      </ul>
    </div>
  );
}
