import React, { useContext, useRef, useEffect, forwardRef } from 'react';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

export const ConfirmDialog = forwardRef((props, _) => {
  const {
    labelConfirm,
    labelCancel,
    iconConfirm,
    iconCancel,
    header,
    visible,
    dialogStyle,
    maximizable,
    onConfirm,
    onHide,
    onPaste,
    children,
    divRef
  } = props;
  const resources = useContext(ResourcesContext);

  const footer = (
    <div>
      <Button label={labelConfirm} icon={iconConfirm ? iconConfirm : 'check'} onClick={onConfirm} />
      <Button
        className="p-button-secondary"
        icon={iconCancel ? iconCancel : 'cancel'}
        label={labelCancel}
        onClick={onHide}
      />
    </div>
  );
  return (
    <div onPaste={onPaste} ref={divRef}>
      <Dialog
        footer={footer}
        header={header}
        maximizable={maximizable}
        onHide={onHide}
        style={dialogStyle ? dialogStyle : { minWidth: '50vw', maxWidth: '80vw', maxHeight: '80vh', minHeight: '30vh' }}
        visible={visible}>
        {children}
      </Dialog>
    </div>
  );
});
