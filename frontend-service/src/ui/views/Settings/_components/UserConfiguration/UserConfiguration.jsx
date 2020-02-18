import React from 'react';
import styles from './userConfiguration.module.scss';

import { ToggleUserConfirmation } from './_components/UserConfirmation';

const UserConfiguration = () => {
  return (
    <div>
      <h1
      // onClick={e => {
      //   userContext.onToggleLogoutConfirm();
      // }}
      >
        UserConfiguration
      </h1>
      <h3>Confirmation user Logout</h3>
      <ToggleUserConfirmation />

      <h3>default amount of pages</h3>
      
    </div>
  );
};

export { UserConfiguration };
