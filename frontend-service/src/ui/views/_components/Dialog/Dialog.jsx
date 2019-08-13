import React from 'react';

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
  return (
    <PrimeDialog
      className={className}
      dismissableMask={dismissableMask}
      footer={footer}
      header={header}
      maximizable={maximizable}
      onHide={onHide}
      style={style ? style : { width: '50vw' }}
      visible={visible}>
      {children}
    </PrimeDialog>
  );
};
