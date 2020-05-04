import React, { useContext } from 'react';

import moment from 'moment';

import styles from './SnapshotItem.module.scss';

import { Button } from 'ui/views/_components/Button';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

export const SnapshotItem = ({ itemData, setIsActiveReleaseSnapshotConfirmDialog, setSnapshotDataToRelease }) => {
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  return (
    <li className={styles.listItem} key={itemData.id}>
      <div className={styles.itemBox}>
        <div className={styles.listItemData}>
          <span className={itemData.isReleased ? `${styles.is_released_snapshot}` : null}>
            {moment(itemData.creationDate).format(
              `${userContext.userProps.dateFormat} ${userContext.userProps.amPm24h ? 'HH' : 'hh'}:mm:ss${
                userContext.userProps.amPm24h ? '' : ' A'
              }`
            )}
          </span>
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
                setIsActiveReleaseSnapshotConfirmDialog(true);
                setSnapshotDataToRelease(itemData);
              }}
            />
          </div>
        </div>
      </div>
      <p className={styles.snapshotDescription}>{itemData.description}</p>
    </li>
  );
};
