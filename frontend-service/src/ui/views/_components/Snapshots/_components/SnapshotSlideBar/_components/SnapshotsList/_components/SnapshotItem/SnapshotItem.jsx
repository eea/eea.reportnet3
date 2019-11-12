import React, { useContext } from 'react';

import moment from 'moment';

import styles from './SnapshotItem.module.scss';

import { Button } from 'ui/views/_components/Button';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { SnapshotContext } from 'ui/views/_components/_context/SnapshotContext';

export function SnapshotItem({ itemData, isReleaseVisible }) {
  const snapshotContext = useContext(SnapshotContext);

  const resources = useContext(ResourcesContext);

  return (
    <li className={styles.listItem}>
      <div className={styles.itemBox}>
        <div className={styles.listItemData}>
          <h5 className={itemData.isReleased ? `${styles.is_released_snapshot}` : null}>
            {moment(itemData.creationDate).format('DD/MM/YYYY HH:mm:ss')}
          </h5>
          <div className={styles.listActions}>
            <Button
              tooltip={resources.messages.restoreSnapshotTooltip}
              tooltipOptions={{ position: 'top' }}
              icon="replay"
              className={`${styles.btn} rp-btn secondary`}
              onClick={() => {
                snapshotContext.snapshotDispatch({
                  type: 'restore_snapshot',
                  payload: { ...itemData }
                });
              }}
            />
            {isReleaseVisible ? (
              <Button
                tooltip={
                  itemData.isReleased
                    ? resources.messages.releasedSnapshotTooltip
                    : resources.messages.releaseSnapshotTooltip
                }
                tooltipOptions={{ position: 'top' }}
                icon={itemData.isReleased ? 'check' : 'cloudUpload'}
                className={`${styles.btn} rp-btn ${itemData.isReleased ? 'success' : `default`}`}
                onClick={() =>
                  snapshotContext.snapshotDispatch(
                    itemData.isReleased
                      ? {}
                      : {
                          type: 'release_snapshot',
                          payload: { ...itemData }
                        }
                  )
                }
              />
            ) : (
              <></>
            )}
            <Button
              tooltip={resources.messages.deleteSnapshotTooltip}
              tooltipOptions={{ position: 'left' }}
              icon="trash"
              disabled={false}
              className={`${styles.btn} rp-btn warning`}
              onClick={() =>
                snapshotContext.snapshotDispatch({
                  type: 'delete_snapshot',
                  payload: { ...itemData }
                })
              }
            />
          </div>
        </div>
      </div>
      <p>{itemData.description}</p>
    </li>
  );
}
