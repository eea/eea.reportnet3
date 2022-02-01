import { memo } from 'react';

import styles from './ErrorMessage.module.scss';

export const ErrorMessage = memo(({ message, className }) => (
  <div className={`${styles.wrapper} ${className ? className : ''}`}>
    <span>{message}</span>
  </div>
));
