import React from 'react';

import styles from './UserImg.module.scss';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

const UserImg = () => {
  return (
    <div>
      <FontAwesomeIcon icon={AwesomeIcons('user-profile')} className={styles.userDataIcon} />
    </div>
  );
};

export { UserImg };
