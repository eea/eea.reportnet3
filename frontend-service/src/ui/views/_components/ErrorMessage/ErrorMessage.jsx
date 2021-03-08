import React from 'react';

import styles from './ErrorMessage.module.scss';

const ErrorMessage = React.memo(({ message }) => {
  return (
    <div className={styles.wrapper}>
      <span>{message}</span>
    </div>
  );
});

export { ErrorMessage };
