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
  itemData,
  showReleaseDialog,
  snapshotDataToRelease,
  snapshotReleasedId
}) => {
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const getSnapshotItemIcon = () =>
    snapshotDataToRelease
      ? itemData.id === snapshotDataToRelease.id
        ? isLoading
          ? 'spinnerAnimate'
          : 'check'
        : 'cloudUpload'
      : itemData.isReleased
      ? isLoading
        ? 'spinnerAnimate'
        : 'check'
      : 'cloudUpload';

  const getSnapshotIconTextStyle = valueItemStyle => {
    if (snapshotDataToRelease) {
      return itemData.id === snapshotDataToRelease.id || snapshotReleasedId !== snapshotDataToRelease.id
        ? itemData.id === snapshotDataToRelease.id
          ? valueItemStyle
            ? 'success'
            : `${styles.is_released_snapshot}`
          : null
        : null;
    } else {
      return itemData.isReleased ? (valueItemStyle ? 'success' : `${styles.is_released_snapshot}`) : ``;
    }
  };

  return (
    <li className={styles.listItem} key={itemData.id}>
      <div className={styles.itemBox}>
        <div className={styles.listItemData}>
          <span className={getSnapshotIconTextStyle(false)}>
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
              className={`${styles.btn} rp-btn ${getSnapshotIconTextStyle(true)}`}
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
