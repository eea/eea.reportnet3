import { useContext } from 'react';

import dayjs from 'dayjs';

import styles from './SnapshotItem.module.scss';

import { Button } from 'views/_components/Button';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { SnapshotContext } from 'views/_functions/Contexts/SnapshotContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

const SnapshotItem = ({ itemData }) => {
  const snapshotContext = useContext(SnapshotContext);
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const getFormatedDate = creationDate => {
    return dayjs(creationDate).format(
      `${userContext.userProps.dateFormat} ${userContext.userProps.amPm24h ? 'HH' : 'hh'}:mm:ss${
        userContext.userProps.amPm24h ? '' : ' A'
      } CET`
    );
  };

  return (
    <li className={styles.listItem}>
      <div className={styles.listItemData}>
        <h5>{getFormatedDate(itemData.creationDate)}</h5>
        {itemData.isReleased && (
          <h5 className={styles.is_released_snapshot}>
            {resources.messages['snapshotIsReleased']}
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
          tooltip={resources.messages.restoreSnapshotTooltip}
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
              ? resources.messages['deleteAutomaticSnapshotTooltip']
              : resources.messages['deleteSnapshotTooltip']
          }
          tooltipOptions={{ position: 'left' }}
        />
      </div>
    </li>
  );
};
export { SnapshotItem };
