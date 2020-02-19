import React, { useContext } from 'react';

import styles from './UserData.module.scss';
import { UserImg } from './UserImg/UserImg';

import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

const UserData = () => {
  const userContext = useContext(UserContext);

  return (
    <div className={styles.userDataContainer}>
      <div className={styles.userLogoBoxContainer}>
        <UserImg></UserImg>
      </div>

      <div className={styles.userName}>{userContext.preferredUsername}</div>

      <div className={styles.userMail}>datacustodian@reportnet.com</div>
    </div>
  );
};

export { UserData };
