import styles from './IconTooltip.module.css';
import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { useState } from 'react';

export const IconTooltip = ({ className = '', levelError, message, style }) => {
  const [isDialogOpen, setIsDialogOpen] = useState(false);

  const openDialog = () => {
    setIsDialogOpen(true);
  };

  const closeDialog = () => {
    setIsDialogOpen(false);
  };

  const buttonProps = {
    className: className,
    icon: '',
    style: style,
    tooltip: message,
    type: 'button',
    onClick: openDialog
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
      buttonProps.onClick = null;
      break;
    default:
      buttonProps.onClick = openDialog;
      break;
  }

  return (
    <>
      <Button {...buttonProps} />
      {isDialogOpen && (
        <Dialog
          className={styles.dialog}
          header={levelError}
          isIconTooltip={true}
          onHide={closeDialog}
          visible={isDialogOpen}>
          <p>
            {message} ({levelError})
          </p>
        </Dialog>
      )}
    </>
  );
};
