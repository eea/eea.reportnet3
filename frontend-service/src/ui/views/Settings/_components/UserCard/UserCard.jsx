import React from 'react';

import styles from './UserCard.module.scss';

import { UserData } from './UserData';

const UserCard = () => {
  return (
    <div className={`${styles.userBoxContainer}`}>
      <UserData />
    </div>
  );
};

export { UserCard };
