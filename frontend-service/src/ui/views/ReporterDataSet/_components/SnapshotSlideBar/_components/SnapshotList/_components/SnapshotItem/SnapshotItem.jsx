import React, { useContext } from 'react';
import moment from 'moment';

import styles from './SnapshotItem.module.scss';

import { Icon } from 'ui/views/_components/Icon';
import { SnapshotContext } from 'ui/views/ReporterDataSet/ReporterDataSet';

export function SnapshotItem({ itemData }) {
  const snapshotContext = useContext(SnapshotContext);

  return (
    <li className={styles.listItem}>
      <div className={styles.itemBox}>
        <div className={styles.listItemData}>
          <h4>{moment(itemData.creationDate).format('DD/MM/YYYY HH:mm:ss')}</h4>
          <div className={styles.listActions}>
            <button
              disabled
              className="rp-btn secondary"
              onClick={() =>
                snapshotContext.snapshotDispatch({
                  type: 'restore_snapshot',
                  payload: { ...itemData }
                })
              }>
              <Icon icon="replay" />
            </button>
            <button
              className="rp-btn warning"
              onClick={() =>
                snapshotContext.snapshotDispatch({
                  type: 'delete_snapshot',
                  payload: { ...itemData }
                })
              }>
              <Icon icon="trash" />
            </button>
          </div>
        </div>
      </div>
      <p>{itemData.description}</p>
    </li>
  );
}
