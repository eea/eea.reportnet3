import { memo, useContext } from 'react';

import isUndefined from 'lodash/isUndefined';

import styles from './UserCard.module.css';

import { routes } from 'conf/routes';
import { getUrl } from 'repositories/_utils/UrlUtils';
import { Icon } from 'views/_components/Icon';
import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';
import { UserService } from 'services/UserService';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';

import { AwesomeIcons } from 'conf/AwesomeIcons';

export const UserCard = memo(() => {
  const notificationContext = useContext(NotificationContext);
  const userContext = useContext(UserContext);

  return (
    <div className={styles.userProfileCard} id="userProfile">
      <div className={styles.userProfile}>
        <a
          href={getUrl(routes.SETTINGS)}
          onClick={async e => {
            e.preventDefault();
          }}
          title="User profile details">
          <FontAwesomeIcon aria-hidden={false} className={styles.avatar} icon={AwesomeIcons('user-profile')} />
          <h5 className={styles.userProfile}>
            {!isUndefined(userContext.preferredUsername) ? userContext.preferredUsername : userContext.name}
          </h5>
        </a>
        <a
          href="#userProfilePage"
          onClick={async e => {
            e.preventDefault();
            userContext.socket.deactivate();
            try {
              await UserService.logout();
            } catch (error) {
              console.error('UserCard - logout.', error);
              notificationContext.add({ type: 'USER_LOGOUT_ERROR' }, true);
            } finally {
              userContext.onLogout();
            }
          }}
          title="logout">
          <Icon icon="logout" />
        </a>
      </div>
    </div>
  );
});
