import React from 'react';
import styles from './userConfiguration.module.scss';
import { DefaultRowsPages } from './_components/defaultRowsPages';
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
      <div>
        <DefaultRowsPages />
      </div>
    </div>
  );
};

export { UserConfiguration };
