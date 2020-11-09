import React, { useContext } from 'react';

import dayjs from 'dayjs';

import styles from './SnapshotItem.module.scss';

import { Button } from 'ui/views/_components/Button';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { SnapshotContext } from 'ui/views/_functions/Contexts/SnapshotContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

const SnapshotItem = ({ itemData }) => {
  const snapshotContext = useContext(SnapshotContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  return (
    <li className={styles.listItem}>
      <div className={styles.listItemData}>
        <h5>
          {dayjs(itemData.creationDate).format(
            `${userContext.userProps.dateFormat} ${userContext.userProps.amPm24h ? 'HH' : 'hh'}:mm:ss${
              userContext.userProps.amPm24h ? '' : ' A'
            }`
          )}
        </h5>
        {itemData.isReleased && (
          <h5 className={styles.is_released_snapshot}>
            {resources.messages['snapshotIsReleased'].toLowerCase()}
            <FontAwesomeIcon icon={AwesomeIcons('released')} />
          </h5>
        )}
        <p className={itemData.isReleased ? `${styles.released_mt}` : null}>{itemData.description}</p>
      </div>

      <div className={styles.listActions}>
        <Button
          tooltip={resources.messages.restoreSnapshotTooltip}
          tooltipOptions={{ position: 'top' }}
          icon="replay"
          className={`${styles.btn} rp-btn secondary`}
          onClick={() => {
            snapshotContext.snapshotDispatch({
              type: 'RESTORE_SNAPSHOT',
              payload: { ...itemData }
            });
          }}
        />

        <Button
          tooltip={
            itemData.isAutomatic
              ? resources.messages['deleteAutomaticSnapshotTooltip']
              : resources.messages['deleteSnapshotTooltip']
          }
          tooltipOptions={{ position: 'left' }}
          icon="trash"
          className={`${styles.btn} rp-btn warning deleteButton ${itemData.isAutomatic && 'p-disabled'}`}
          onClick={() =>
            itemData.isAutomatic
              ? {}
              : snapshotContext.snapshotDispatch({
                  type: 'DELETE_SNAPSHOT',
                  payload: { ...itemData }
                })
          }
        />
      </div>
    </li>
  );
};
export { SnapshotItem };
