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
          label={labelCancel}
          icon={iconCancel ? iconCancel : 'cancel'}
          onClick={onHide}
          className="p-button-secondary"
        />
      </div>
    );

    return (
      <Dialog
        header={header}
        visible={visible}
        style={dialogStyle ? dialogStyle : { width: '50vw' }}
        footer={footer}
        onHide={onHide}
        maximizable={maximizable}>
        {children}
      </Dialog>
    );
  }
);
