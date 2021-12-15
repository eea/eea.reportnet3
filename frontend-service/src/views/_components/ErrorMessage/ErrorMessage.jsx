import { memo } from 'react';

import styles from './ErrorMessage.module.scss';

const ErrorMessage = memo(({ message, classNames }) => {
  return (
    <div className={`${classNames} ${styles.wrapper}`}>
      <span>{message}</span>
    </div>
  );
});

export { ErrorMessage };
