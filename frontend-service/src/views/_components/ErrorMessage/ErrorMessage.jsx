import { memo } from 'react';

import styles from './ErrorMessage.module.scss';

export const ErrorMessage = memo(({ className, message }) => (
  <div className={`${styles.wrapper} ${className ? className : ''}`}>
    <span>{message}</span>
  </div>
));
