import React from 'react';

import styles from './SnapshotsList.module.scss';

import { SnapshotItem } from './_components/SnapshotItem';

const SnapshotsList = ({ snapshotListData, isReleaseVisible }) => {
  return (
    <div className={`${styles.listContainer}  ${styles.section}`}>
      <ul>
        {snapshotListData.map(item => (
          <SnapshotItem itemData={item} key={item.id} isReleaseVisible={isReleaseVisible} />
        ))}
      </ul>
    </div>
  );
};
export { SnapshotsList };
