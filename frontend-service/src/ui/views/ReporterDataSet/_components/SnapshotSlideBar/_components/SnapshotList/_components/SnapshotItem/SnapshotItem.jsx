import React, { useContext } from 'react';

import moment from 'moment';

import styles from './SnapshotItem.module.scss';

import { Button } from 'ui/views/_components/Button';
import { SnapshotContext } from 'ui/views/ReporterDataSet/ReporterDataSet';

export function SnapshotItem({ itemData }) {
  const snapshotContext = useContext(SnapshotContext);

  return (
    <li className={styles.listItem}>
      <div className={styles.itemBox}>
        <div className={styles.listItemData}>
          <h4>{moment(itemData.creationDate).format('DD/MM/YYYY HH:mm:ss')}</h4>
          <div className={styles.listActions}>
            <Button
              icon="replay"
              layout="simple"
              disabled={true}
              className="rp-btn secondary"
              onClick={() =>
                snapshotContext.snapshotDispatch({
                  type: 'restore_snapshot',
                  payload: { ...itemData }
                })
              }
            />
            <Button
              icon="trash"
              layout="simple"
              disabled={false}
              className="rp-btn warning"
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
