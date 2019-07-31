import React, { useContext } from 'react';

import { Button } from 'primereact/button';
import { Dialog } from 'primereact/dialog';
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
        <Button label={labelConfirm} icon={iconConfirm ? iconConfirm : resources.icons['check']} onClick={onConfirm} />
        <Button
          label={labelCancel}
          icon={iconCancel ? iconCancel : resources.icons['cancel']}
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
