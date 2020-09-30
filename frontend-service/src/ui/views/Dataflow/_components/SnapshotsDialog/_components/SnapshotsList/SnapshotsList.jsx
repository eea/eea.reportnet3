import React, { useContext } from 'react';

import { isEmpty } from 'lodash';

import styles from './SnapshotsList.module.scss';

import { SnapshotItem } from './_components/SnapshotItem';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const SnapshotsList = ({
  getSnapshotData,
  isLoading,
  showReleaseDialog,
  snapshotsListData,
  snapshotDataToRelease
}) => {
  const resources = useContext(ResourcesContext);

  if (!isEmpty(snapshotsListData)) {
    return (
      <div className={`${styles.listContainer}  ${styles.section}`}>
        <ul>
          {snapshotsListData.map(item => (
            <div key={item.id}>
              <SnapshotItem
                getSnapshotData={getSnapshotData}
                isLoading={isLoading}
                itemData={item}
                showReleaseDialog={showReleaseDialog}
                snapshotDataToRelease={snapshotDataToRelease}
              />
            </div>
          ))}
        </ul>
      </div>
    );
  } else {
    return <h3>{resources.messages['emptySnapshotList']}</h3>;
  }
};
