import React from 'react';

import styles from './UserDesignOptions.module.scss';

const UserDesignOptions = () => {
  return (
    <div className={styles.userDesignContainer}>
      <div className={styles.userLogoBoxContainer}></div>

      <div className={styles.userName}>Theme</div>

      <div className={styles.userMail}>Theme</div>
    </div>
  );
};

export { UserDesignOptions };
