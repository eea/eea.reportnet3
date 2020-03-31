import React from 'react';
import styles from './userConfiguration.module.scss';
import { DefaultRowsPages } from './_components/defaultRowsPages';
import { ToggleUserConfirmation } from './_components/UserConfirmation';
import { DateFormat } from './_components/DateFormat';
const UserConfiguration = () => {
  return (
    <div className={styles.userConfigurationContainer}>
      <div className={styles.userConfirmLogout}>
        <ToggleUserConfirmation />
      </div>

      <div className={styles.userConfirmLogout}>
        <DefaultRowsPages />
      </div>
      <div className={styles.userConfirmLogout}>
        <DateFormat />
      </div>
    </div>
  );
};

export { UserConfiguration };
