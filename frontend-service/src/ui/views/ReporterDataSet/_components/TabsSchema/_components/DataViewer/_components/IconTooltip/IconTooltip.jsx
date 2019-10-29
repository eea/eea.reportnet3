import React, { useContext } from 'react';

import styles from './IconTooltip.module.css';

import { Button } from 'ui/views/_components/Button';

export const IconTooltip = ({ levelError, message, style }) => {
  const buttonProps = {
    type: 'button',
    icon: '',
    tooltip: message,
    className: '',
    style: style
  };

  switch (levelError) {
    case 'WARNING':
      buttonProps.icon = 'warning';
      buttonProps.className = `${styles.buttonCustom} ${styles.warning}`;
      break;
    case 'ERROR':
      buttonProps.icon = 'warning';
      buttonProps.className = `${styles.buttonCustom} ${styles.error}`;
      break;
    case 'BLOCKER':
      buttonProps.icon = 'banned';
      buttonProps.className = `${styles.buttonCustom} ${styles.blocker}`;
      break;
    case '':
      buttonProps.icon = '';
      buttonProps.className = '';
      break;
    default:
      break;
  }

  return <Button {...buttonProps} />;
};
