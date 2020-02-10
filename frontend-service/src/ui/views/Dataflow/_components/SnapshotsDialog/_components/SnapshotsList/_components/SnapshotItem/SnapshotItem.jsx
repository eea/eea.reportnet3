import React, { useContext } from 'react';

import moment from 'moment';

import styles from './SnapshotItem.module.scss';

import { Button } from 'ui/views/_components/Button';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const SnapshotItem = ({ getSnapshotData, isLoading, itemData, showReleaseDialog }) => {
  const resources = useContext(ResourcesContext);

  return (
    <li className={styles.listItem} key={itemData.id}>
      <div className={styles.itemBox}>
        <div className={styles.listItemData}>
          <span className={itemData.isReleased ? `${styles.is_released_snapshot}` : null}>
            {moment(itemData.creationDate).format('YYYY-MM-DD HH:mm:ss')}
            {!itemData.isValid && (
              <Button
                className={`${styles.btn} rp-btn ${styles.hasBlockers}`}
                icon="warning"
                onClick={() => {}}
                tooltip={resources.messages['recordBlockers']}
                tooltipOptions={{ position: 'right' }}
              />
            )}
          </span>
          <div className={styles.listActions}>
            <Button
              tooltip={
                itemData.isReleased
                  ? resources.messages.releasedSnapshotTooltip
                  : resources.messages.releaseSnapshotTooltip
              }
              disabled={isLoading || !itemData.isValid}
              tooltipOptions={{ position: 'right' }}
              icon={itemData.isReleased ? (isLoading ? 'spinnerAnimate' : 'check') : 'cloudUpload'}
              className={`${styles.btn} rp-btn ${itemData.isReleased ? 'success' : `default`}`}
              onClick={() => {
                showReleaseDialog({ isReleased: false });
                getSnapshotData(itemData);
              }}
            />
          </div>
        </div>
      </div>
      <p className={styles.snapshotDescription}>{itemData.description}</p>
    </li>
  );
};
