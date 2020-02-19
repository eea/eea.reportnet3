import React from 'react';

import styles from './UserCard.module.scss';

import { UserData } from './UserData';
import { UserObligations } from './UserObligations';
import { UserLinks } from './UserLinks';

const UserCard = () => {
  return (
    <div className={`${styles.userBoxContainer}`}>
      <UserData />
      <UserObligations />
      <UserLinks />
    </div>
  );
};

export { UserCard };
