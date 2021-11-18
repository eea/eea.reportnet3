import styles from './IconTooltip.module.css';

import { Button } from 'views/_components/Button';

export const IconTooltip = ({ className = '', levelError, message, style }) => {
  const buttonProps = {
    className: className,
    icon: '',
    style: style,
    tooltip: message,
    type: 'button'
  };

  switch (levelError) {
    case 'INFO':
      buttonProps.icon = 'warning';
      buttonProps.className = `${styles.buttonCustom} ${styles.info} ${className}`;
      break;
    case 'WARNING':
      buttonProps.icon = 'warning';
      buttonProps.className = `${styles.buttonCustom} ${styles.warning} ${className}`;
      break;
    case 'ERROR':
      buttonProps.icon = 'warning';
      buttonProps.className = `${styles.buttonCustom} ${styles.error} ${className}`;
      break;
    case 'BLOCKER':
      buttonProps.icon = 'blocker';
      buttonProps.className = `${styles.buttonCustom} ${styles.blocker} ${className}`;
      break;
    case '':
      buttonProps.icon = '';
      buttonProps.className = ` ${className}`;
      break;
    default:
      break;
  }

  return <Button {...buttonProps} />;
};
