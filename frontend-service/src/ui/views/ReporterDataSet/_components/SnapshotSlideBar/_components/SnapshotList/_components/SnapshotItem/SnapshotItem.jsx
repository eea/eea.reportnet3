import React, { useContext } from 'react';
import moment from 'moment';

import styles from './SnapshotItem.module.scss';

import { IconComponent } from 'ui/views/_components/IconComponent';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { SnapshotContext } from 'ui/views/ReporterDataSet/ReporterDataSet';

export function SnapshotItem({ itemData }) {
  const snapshotContext = useContext(SnapshotContext);
  const resources = useContext(ResourcesContext);

  return (
    <li className={styles.listItem}>
      <div className={styles.listItemData}>
        <h4>{moment(itemData.creationDate).format('DD/MM/YYYY HH:mm:ss')}</h4>
        <p>{itemData.description}</p>
      </div>
      <div className={styles.listActions}>
        <button
          className="rp-btn success"
          onClick={() =>
            snapshotContext.snapshotDispatch({
              type: 'restore_snapshot',
              payload: { ...itemData }
            })
          }>
          <IconComponent icon={resources.icons['replay']} />
        </button>
        <button
          className="rp-btn warning"
          onClick={() =>
            snapshotContext.snapshotDispatch({
              type: 'delete_snapshot',
              payload: { ...itemData }
            })
          }>
          <IconComponent icon={resources.icons['trash']} />
        </button>
      </div>
    </li>
  );
}
