import { memo } from 'react';

import styles from './ErrorMessage.module.scss';

const ErrorMessage = memo(({ message }) => {
  return (
    <div className={styles.wrapper}>
      <span>{message}</span>
    </div>
  );
});

export { ErrorMessage };
