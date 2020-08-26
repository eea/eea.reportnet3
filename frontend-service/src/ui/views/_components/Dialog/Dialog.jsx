import React, { useContext, useEffect, useState } from 'react';
import uuid from 'uuid';

import styles from './Dialog.module.scss';

import { Dialog as PrimeDialog } from 'primereact/dialog';

import { DialogContext } from 'ui/views/_functions/Contexts/DialogContext';

export const Dialog = ({
  blockScroll = true,
  children,
  className,
  closeOnEscape = false,
  contentStyle,
  focusOnShow = true,
  footer,
  header,
  onHide,
  style,
  visible,
  zIndex = 5000
}) => {
  const dialogContext = useContext(DialogContext);
  const [dialogId, setDialogId] = useState('');
  const [maskStyle, setMaskStyle] = useState({
    display: visible ? 'flex' : 'none',
    flexDirection: 'column',
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: zIndex
  });

  const dialogStyle = {
    top: 'auto',
    left: 'auto',
    zIndex: zIndex
  };

  useEffect(() => {
    const newDialogId = uuid.v4();
    setDialogId(newDialogId);
    dialogContext.add(newDialogId);
    return () => {
      dialogContext.remove(dialogId);
      restoreBodyScroll();
    };
  }, []);

  const restoreBodyScroll = () => {
    if (dialogContext.open.length === 0) {
      document.body.style.overflow = 'hidden auto';
    }
  };

  useEffect(() => {
    const body = document.querySelector('body');
    visible && (body.style.overflow = 'hidden');
  }, [visible]);

  useEffect(() => {
    if (dialogContext.open.indexOf(dialogId) >= 0) {
      setMaskStyle({
        ...maskStyle,
        zIndex: zIndex + dialogContext.open.indexOf(dialogId)
      });
    }
  }, [dialogContext.open]);
  return (
    <div className={styles.dialog_mask_wrapper} style={maskStyle}>
      <PrimeDialog
        blockScroll={blockScroll}
        className={className}
        closeOnEscape={closeOnEscape}
        contentStyle={contentStyle}
        focusOnShow={focusOnShow}
        footer={footer}
        header={header}
        maximizable={false}
        onHide={onHide}
        style={style ? { ...style, ...dialogStyle } : { ...dialogStyle, width: '50vw' }}
        visible={visible}>
        {children}
      </PrimeDialog>
    </div>
  );
};
