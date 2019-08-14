import React, { useContext } from 'react';

import styles from './UserCard.module.css';

import { Icon } from 'ui/views/_components/Icon';

const UserCard = React.memo(() => {
  return (
    <div id="userProfile" className={styles.userProfileCard}>
      <div className={styles.userProfile}>
        <a href="#userProfilePage" title="Edit user profile">
          <img className={styles.avatar} alt="User Profile" src="https://image.flaticon.com/icons/svg/149/149071.svg" />
          <h5 className={styles.userProfile}>User</h5>
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
