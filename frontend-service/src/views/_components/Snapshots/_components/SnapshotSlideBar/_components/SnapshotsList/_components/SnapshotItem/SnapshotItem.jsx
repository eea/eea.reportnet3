import { useContext } from 'react';

import styles from './SnapshotItem.module.scss';

import { Button } from 'views/_components/Button';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { SnapshotContext } from 'views/_functions/Contexts/SnapshotContext';

import { useDateTimeFormatByUserPreferences } from 'views/_functions/Hooks/useDateTimeFormatByUserPreferences';

const SnapshotItem = ({ itemData }) => {
  const snapshotContext = useContext(SnapshotContext);
  const resourcesContext = useContext(ResourcesContext);

  const { getDateTimeFormatByUserPreferences } = useDateTimeFormatByUserPreferences();

  return (
    <li className={styles.listItem}>
      <div className={styles.listItemData}>
        <h5>{getDateTimeFormatByUserPreferences(itemData.creationDate)} CET</h5>
        {itemData.isReleased && (
          <h5 className={styles.is_released_snapshot}>
            {resourcesContext.messages['snapshotIsReleased']}
            <FontAwesomeIcon icon={AwesomeIcons('released')} />
          </h5>
        )}
        <p className={itemData.isReleased ? `${styles.released_mt}` : null}>{itemData.description}</p>
      </div>

      <div className={styles.listActions}>
        <Button
          className={`${styles.btn} rp-btn secondary`}
          icon="replay"
          onClick={() => {
            snapshotContext.snapshotDispatch({
              type: 'RESTORE_SNAPSHOT',
              payload: { ...itemData }
            });
          }}
          tooltip={resourcesContext.messages.restoreSnapshotTooltip}
          tooltipOptions={{ position: 'top' }}
        />

        <Button
          className={`${styles.btn} rp-btn warning deleteButton ${itemData.isAutomatic && 'p-disabled'}`}
          icon="trash"
          onClick={() =>
            itemData.isAutomatic
              ? {}
              : snapshotContext.snapshotDispatch({
                  type: 'DELETE_SNAPSHOT',
                  payload: { ...itemData }
                })
          }
          tooltip={
            itemData.isAutomatic
              ? resourcesContext.messages['deleteAutomaticSnapshotTooltip']
              : resourcesContext.messages['deleteSnapshotTooltip']
          }
          tooltipOptions={{ position: 'left' }}
        />
      </div>
    </li>
  );
};
export { SnapshotItem };
