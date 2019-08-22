import React, { useContext } from 'react';

import styles from './CustomIconTooltip.module.css';

import { Button } from 'primereact/button';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

export const CustomIconTooltip = ({ levelError, message }) => {
  let validationIcon = '';
  let iconColor = '';

  const resources = useContext(ResourcesContext);

  switch (levelError) {
    case 'WARNING':
      validationIcon = resources.icons['warning'];
      iconColor = '#ffd617';
      break;
    case 'ERROR':
      validationIcon = resources.icons['warning'];
      iconColor = '#da2131';
      break;
    case 'BLOCKER':
      validationIcon = resources.icons['banned'];
      iconColor = '#da2131';
      break;
    case '':
      validationIcon = '';
      iconColor = '';
      break;
    default:
      break;
  }

  return (
    <Button
      type="button"
      icon={validationIcon}
      tooltip={message}
      className={styles.buttonCustom}
      style={{ color: `${iconColor}` }}
    />
  );
};
