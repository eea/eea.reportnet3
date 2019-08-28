import React, { useContext } from 'react';

import { isUndefined } from 'lodash';

import styles from './UserCard.module.css';

import { Icon } from 'ui/views/_components/Icon';

import { UserContext } from 'ui/views/_components/_context/UserContext';
import { UserService } from 'core/services/User';

const UserCard = React.memo(() => {
  const user = useContext(UserContext);
  return (
    <div id="userProfile" className={styles.userProfileCard}>
      <div className={styles.userProfile}>
        <a
          href="#userProfilePage"
          title="User profile details"
          onClick={async e => {
            e.preventDefault();
          }}>
          <img className={styles.avatar} alt="User Profile" src="https://image.flaticon.com/icons/svg/149/149071.svg" />
          <h5 className={styles.userProfile}>
            {!isUndefined(user.preferredUsername) ? user.preferredUsername : user.name}
          </h5>
        </a>
        <a
          href="#userProfilePage"
          title="logout"
          onClick={async e => {
            e.preventDefault();
            try {
              const logout = await UserService.logout();
            } catch (error) {
              console.error(error);
            } finally {
              user.onLogout();
            }
          }}>
          <Icon icon="logout" />
        </a>
      </div>
      {/* <div className={styles.logOut}>
                <a href="#logOut" title="Log out">
                  <Icon icon="logout" />
                </a>
            </div> */}
    </div>
  );
});

export { UserCard };
