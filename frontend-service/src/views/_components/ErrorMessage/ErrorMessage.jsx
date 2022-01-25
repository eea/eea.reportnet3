import { memo } from 'react';

import styles from './ErrorMessage.module.scss';

export const ErrorMessage = memo(({ message }) => (
  <div className={styles.wrapper}>
    <span>{message}</span>
  </div>
));
