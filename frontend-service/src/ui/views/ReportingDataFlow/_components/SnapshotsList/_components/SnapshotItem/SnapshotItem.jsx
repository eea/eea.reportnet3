import React, { useContext } from 'react';

import moment from 'moment';

import styles from './SnapshotItem.module.scss';

import { Button } from 'ui/views/_components/Button';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

import { SnapshotService } from 'core/services/Snapshot';

export function SnapshotItem({ itemData, dataflowId, datasetId, onLoadSnapshotList }) {
  const resources = useContext(ResourcesContext);

  const onReleaseSnapshot = async snapShotId => {
    const snapshotToRelease = await SnapshotService.releaseById(dataflowId, datasetId, snapShotId);

    if (snapshotToRelease.isReleased) {
      onLoadSnapshotList(datasetId);
    }
  };

  return (
    <li className={styles.listItem} key={itemData.id}>
      <div className={styles.itemBox}>
        <div className={styles.listItemData}>
          <h5 className={itemData.isReleased ? `${styles.is_released_snapshot}` : null}>
            {moment(itemData.creationDate).format('DD/MM/YYYY HH:mm:ss')}
          </h5>
          <div className={styles.listActions}>
            <Button
              tooltip={
                itemData.isReleased
                  ? resources.messages.releasedSnapshotTooltip
                  : resources.messages.releaseSnapshotTooltip
              }
              tooltipOptions={{ position: 'right' }}
              icon={itemData.isReleased ? 'check' : 'cloudUpload'}
              className={`${styles.btn} rp-btn ${itemData.isReleased ? 'success' : `default`}`}
              onClick={() => {
                onReleaseSnapshot(itemData.id);
              }}
            />
          </div>
        </div>
      </div>
      <p>{itemData.description}</p>
    </li>
  );
}
