import React, { useContext } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';
import { routes } from 'ui/routes';
import { isUndefined } from 'lodash';
import { getUrl } from 'core/infrastructure/CoreUtils';
import styles from './UserCard.module.css';

import { Icon } from 'ui/views/_components/Icon';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { UserService } from 'core/services/User';

const UserCard = React.memo(() => {
  const notificationContext = useContext(NotificationContext);
  const userContext = useContext(UserContext);
  return (
    <div id="userProfile" className={styles.userProfileCard}>
      <div className={styles.userProfile}>
        <a
          href={getUrl(routes.SETTINGS)}
          title="User profile details"
          onClick={async e => {
            e.preventDefault();
          }}>
          <FontAwesomeIcon aria-hidden={false} className={styles.avatar} icon={AwesomeIcons('user-profile')} />
          <h5 className={styles.userProfile}>
            {!isUndefined(userContext.preferredUsername) ? userContext.preferredUsername : userContext.name}
          </h5>
        </a>
        <a
          href="#userProfilePage"
          title="logout"
          onClick={async e => {
            e.preventDefault();
            userContext.socket.deactivate();
            try {
              await UserService.logout();
            } catch (error) {
              notificationContext.add({
                type: 'USER_LOGOUT_ERROR'
              });
            } finally {
              userContext.onLogout();
            }
          }}>
          <Icon icon="logout" />
        </a>
      </div>
    </div>
  );
});

export { UserCard };
