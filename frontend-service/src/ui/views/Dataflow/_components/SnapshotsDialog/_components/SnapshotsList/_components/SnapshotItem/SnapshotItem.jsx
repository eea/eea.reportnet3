import React, { useContext } from 'react';

import moment from 'moment';

import { isEmpty } from 'lodash';

import styles from './SnapshotItem.module.scss';

import { Button } from 'ui/views/_components/Button';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

export const SnapshotItem = ({
  getSnapshotData,
  isLoading,
  isSnapshotListCreatedReleaseLoading,
  itemData,
  showReleaseDialog,
  snapshotIdToRelease
}) => {
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const getSnapshotItemIcon = () => {
    if (snapshotIdToRelease) {
      return itemData.id === snapshotIdToRelease ? (isLoading ? 'spinnerAnimate' : 'check') : 'cloudUpload';
    } else {
      return itemData.isReleased ? (isSnapshotListCreatedReleaseLoading ? 'spinnerAnimate' : 'check') : 'cloudUpload';
    }
  };

  const getSnapshotTextStyle = () => {
    if (snapshotIdToRelease !== '') {
      if (itemData.id === snapshotIdToRelease) {
        return `${styles.is_released_snapshot}`;
      } else if (itemData.isReleased && itemData.id !== snapshotIdToRelease) {
        return itemData.id === snapshotIdToRelease ? `${styles.is_released_snapshot}` : ``;
      }
    } else if (itemData.isReleased) {
      return isSnapshotListCreatedReleaseLoading ? `` : `${styles.is_released_snapshot}`;
    } else {
      return ``;
    }
  };

  const getSnapshotIconColour = () => {
    if (snapshotIdToRelease !== '') {
      if (itemData.id === snapshotIdToRelease) {
        return 'success';
      } else if (itemData.isReleased && itemData.id !== snapshotIdToRelease) {
        return itemData.isReleased && itemData.id === snapshotIdToRelease ? 'success' : null;
      }
    } else if (itemData.isReleased) {
      if (isSnapshotListCreatedReleaseLoading) {
        return null;
      } else {
        return 'success';
      }
    } else {
      return null;
    }
  };

  return (
    <li className={styles.listItem} key={itemData.id}>
      <div className={styles.itemBox}>
        <div className={styles.listItemData}>
          <span className={getSnapshotTextStyle()}>
            {moment(itemData.creationDate).format(
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
          </span>
          <div className={styles.listActions}>
            <Button
              className={`${styles.btn} rp-btn ${getSnapshotIconColour()}`}
              disabled={isLoading || itemData.isBlocked}
              icon={getSnapshotItemIcon()}
              onClick={() => {
                showReleaseDialog({ isReleased: false });
                getSnapshotData(itemData);
              }}
              tooltip={
                itemData.isReleased
                  ? resources.messages['releasedSnapshotTooltip']
                  : resources.messages['releaseSnapshotTooltip']
              }
              tooltipOptions={{ position: 'right' }}
            />
          </div>
        </div>
      </div>
      <p className={styles.snapshotDescription}>{itemData.description}</p>
    </li>
  );
};
