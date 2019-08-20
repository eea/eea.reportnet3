import React, { useContext } from 'react';

import moment from 'moment';

import styles from './SnapshotItem.module.scss';

import { Button } from 'ui/views/_components/Button';
import { SnapshotContext } from 'ui/views/ReporterDataSet/ReporterDataSet';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

export function SnapshotItem({ itemData }) {
  const snapshotContext = useContext(SnapshotContext);

  const resources = useContext(ResourcesContext);

  return (
    <li className={styles.listItem}>
      <div className={styles.itemBox}>
        <div className={styles.listItemData}>
          <h4>{moment(itemData.creationDate).format('DD/MM/YYYY HH:mm:ss')}</h4>
          <div className={styles.listActions}>
            <Button
              tooltip={resources.messages.restoreSnapshotTooltip}
              tooltipOptions={{ position: 'top' }}
              icon="replay"
              disabled={true}
              className={`${styles.btn} rp-btn secondary`}
              onClick={() =>
                snapshotContext.snapshotDispatch({
                  type: 'restore_snapshot',
                  payload: { ...itemData }
                })
              }
            />
            <Button
              tooltip={resources.messages.releaseSnapshotTooltip}
              tooltipOptions={{ position: 'top' }}
              icon="cloudUpload"
              disabled={false}
              className={`${styles.btn} rp-btn secondary`}
              onClick={() =>
                snapshotContext.snapshotDispatch({
                  type: 'release_snapshot',
                  payload: { ...itemData }
                })
              }
            />
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
