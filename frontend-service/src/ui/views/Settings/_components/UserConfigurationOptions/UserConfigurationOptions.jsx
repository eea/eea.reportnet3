import React, { useContext } from 'react';
import styles from './userConfiguration.module.scss';

import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

const UserConfigurationOptions = () => {
  // const userContext = useContext(UserContext);
  // return (
  //   <h1
  //     onClick={e => {
  //       userContext.onToggleLogoutConfirm();
  //     }}>
  //     UserConfiguration
  //   </h1>
  // );
  return (
    <div className={styles.userConfiguration}>
      <div className={styles.userLogoBoxContainer}></div>

      <div className={styles.userName}>Theme</div>

      <div className={styles.userMail}>Theme</div>
    </div>
  );
};

export { UserConfigurationOptions };
