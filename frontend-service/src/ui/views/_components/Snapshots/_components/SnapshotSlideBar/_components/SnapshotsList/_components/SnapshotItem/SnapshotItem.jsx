import React, { useContext } from 'react';

import dayjs from 'dayjs';

import styles from './SnapshotItem.module.scss';

import { Button } from 'ui/views/_components/Button';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { SnapshotContext } from 'ui/views/_functions/Contexts/SnapshotContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

const SnapshotItem = ({ itemData, isReleaseVisible }) => {
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
          {itemData.isBlocked && (
            <Button
              className={`${styles.btn} rp-btn ${styles.hasBlockers}`}
              icon="warning"
              onClick={() => {}}
              tooltip={resources.messages['recordBlockers']}
              tooltipOptions={{ position: 'right' }}
            />
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
                      type: 'RELEASE_SNAPSHOT',
                      payload: { ...itemData }
                    }
              )
            }
          />
        ) : (
          <></>
        )}

        {!itemData.isReleased /* || itemData.whereReleased --- */&& (
          <Button
            tooltip={resources.messages.deleteSnapshotTooltip}
            tooltipOptions={{ position: 'left' }}
            icon="trash"
            disabled={itemData.isReleased}
            className={`${styles.btn} rp-btn warning`}
            onClick={() =>
              snapshotContext.snapshotDispatch({
                type: 'DELETE_SNAPSHOT',
                payload: { ...itemData }
              })
            }
          />
        )}
      </div>
    </li>
  );
};
export { SnapshotItem };
