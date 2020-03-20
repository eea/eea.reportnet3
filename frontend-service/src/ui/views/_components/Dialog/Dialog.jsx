import React, { useEffect } from 'react';

import styles from './Dialog.module.scss';

import { Dialog as PrimeDialog } from 'primereact/dialog';

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
  focusOnShow,
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
  zIndex = 3050
}) => {
  const maskStyle = {
    display: visible ? 'flex' : 'none',
    flexDirection: 'column',
    justifyContent: 'center',
    alignItems: 'center',
    zIndex
  };
  const dialogStyle = {
    top: 'auto',
    left: 'auto',
    zIndex
  };
  useEffect(() => {
    const body = document.querySelector('body');
    if (visible) {
      body.style.overflow = 'hidden';
    } else {
      body.style.overflow = 'hidden auto';
    }
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
