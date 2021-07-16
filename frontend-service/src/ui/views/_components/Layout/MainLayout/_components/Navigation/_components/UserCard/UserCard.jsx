import { memo, useContext } from 'react';

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

const UserCard = memo(() => {
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
              notificationContext.add({
                type: 'USER_LOGOUT_ERROR'
              });
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

export { UserCard };
