import React from 'react';
import styles from './userConfiguration.module.scss';
import { DefaultRowsPages } from './_components/defaultRowsPages';
import { ToggleUserConfirmation } from './_components/UserConfirmation';

const UserConfiguration = () => {
  return (
    <div className={styles.userConfigurationContainer}>
     
      <div className={styles.userConfirmLogout}>
      <ToggleUserConfirmation />
      </div>

      <div className={styles.userConfirmLogout}>
        <DefaultRowsPages />
      </div>
    </div>
   );
 
};

export { UserConfiguration };
