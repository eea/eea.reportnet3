import React from 'react';

import styles from './Dialog.module.scss';

import { Dialog as PrimeDialog } from 'primereact/dialog';

export const Dialog = ({
  appendTo,
  baseZIndex,
  blockScroll,
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
  visible
}) => {
  const maskStyle = {
    display: visible ? 'flex' : 'none',
    flexDirection: 'column',
    justifyContent: 'center',
    alignItems: 'center'
    // zIndex: 1050
  };
  const dialogStyle = {
    top: 'auto',
    left: 'auto'
    // zIndex: 1050
  };
  return (
    <div className={styles.dialog_mask_wrapper} style={maskStyle}>
      <PrimeDialog
        className={className}
        closeOnEscape={closeOnEscape}
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
