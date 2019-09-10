import React, { useContext } from 'react';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

export const ConfirmDialog = React.memo(
  ({
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
    children
  }) => {
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
      <Dialog
        footer={footer}
        header={header}
        maximizable={maximizable}
        onHide={onHide}
        style={dialogStyle ? dialogStyle : { minWidth: '50vw', maxWidth: '70vw' }}
        visible={visible}>
        {children}
      </Dialog>
    );
  }
);
