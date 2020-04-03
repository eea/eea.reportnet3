import React from 'react';
import styles from './userConfiguration.module.scss';
import { DefaultRowsPages } from './_components/defaultRowsPages';
import { ToggleUserConfirmation } from './_components/UserConfirmation';
import { DateFormat } from './_components/DateFormat';
const UserConfiguration = props => {
  return (
    <div className={styles.userConfigurationContainer}>
      <div className={styles.userConfirmLogout}>
        <ToggleUserConfirmation Attr={props.Attributes} />
      </div>

      <div className={styles.userConfirmLogout}>
        <DefaultRowsPages Attr={props.Attributes} />
      </div>
      <div className={styles.userConfirmLogout}>
        <DateFormat Attr={props.Attributes} />
      </div>
    </div>
  );
};

export { UserConfiguration };
