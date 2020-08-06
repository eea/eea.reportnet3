import React, { useEffect, useContext } from 'react';

import styles from './Dialog.module.scss';

import { Dialog as PrimeDialog } from 'primereact/dialog';

import { DialogContext } from 'ui/views/_functions/Contexts/DialogContext';

export const Dialog = ({
  appendTo,
  baseZIndex,
  blockScroll = true,
  children,
  className,
  closable,
  closeOnEscape,
  contentStyle,
  dismissableMask,
  focusOnShow = true,
  footer,
  header,
  id,
  maximizable,
  modal,
  onHide,
  onShow,
  rtl,
  showHeader,
  style,
  visible,
  zIndex = 5000
}) => {
  const dialogContext = useContext(DialogContext);

  const maskStyle = {
    display: visible ? 'flex' : 'none',
    flexDirection: 'column',
    justifyContent: 'center',
    alignItems: 'center',
    zIndex: zIndex + dialogContext.open
  };

  const dialogStyle = {
    top: 'auto',
    left: 'auto',
    zIndex: zIndex + dialogContext.open
  };

  useEffect(() => {
    dialogContext.add();
    return () => {
      dialogContext.remove();
    };
  }, []);

  useEffect(() => {
    const body = document.querySelector('body');
    visible && (body.style.overflow = 'hidden');
    dialogContext.add();

    return () => {
      if (dialogContext.open === 1) {
        body.style.overflow = 'hidden auto';
      }
    };
  }, [visible]);
  return (
    <div className={styles.dialog_mask_wrapper} style={maskStyle}>
      <PrimeDialog
        blockScroll={blockScroll}
        className={className}
        closeOnEscape={closeOnEscape}
        contentStyle={contentStyle}
        dismissableMask={dismissableMask}
        focusOnShow={focusOnShow}
        footer={footer}
        header={header}
        maximizable={maximizable}
        onHide={onHide}
        style={style ? { ...style, ...dialogStyle } : { ...dialogStyle, width: '50vw' }}
        visible={visible}>
        {children}
      </PrimeDialog>
    </div>
  );
};
