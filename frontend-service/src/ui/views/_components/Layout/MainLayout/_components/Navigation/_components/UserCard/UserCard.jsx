import React, { useContext } from 'react';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { isUndefined } from 'lodash';

import styles from './UserCard.module.css';

import { Icon } from 'ui/views/_components/Icon';

import { UserContext } from 'ui/views/_functions/Contexts/UserContext';
import { UserService } from 'core/services/User';

const UserCard = React.memo(() => {
  const userContext = useContext(UserContext);
  return (
    <div id="userProfile" className={styles.userProfileCard}>
      <div className={styles.userProfile}>
        <a
          href="#userProfilePage"
          title="User profile details"
          onClick={async e => {
            e.preventDefault();
          }}>
          <FontAwesomeIcon className={styles.avatar} icon={AwesomeIcons('user-profile')} />
          <h5 className={styles.userProfile}>
            {!isUndefined(userContext.preferredUsername) ? userContext.preferredUsername : userContext.name}
          </h5>
        </a>
        <a
          href="#userProfilePage"
          title="logout"
          onClick={async e => {
            e.preventDefault();
            userContext.socket.disconnect(() => {});
            try {
              await UserService.logout();
            } catch (error) {
              console.error(error);
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
