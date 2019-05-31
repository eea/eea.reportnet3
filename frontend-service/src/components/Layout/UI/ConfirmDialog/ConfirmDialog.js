import React, { useContext} from 'react';
import { Dialog } from 'primereact/dialog';
import { Button } from 'primereact/button';

const ConfirmDialog = (props) =>{

    const footer = (
        <div>
            <Button label={props.labelConfirm} icon={(props.iconConfirm) ? props.iconConfirm : "pi pi-check"} onClick={props.onConfirm} />
            <Button label={props.labelCancel} icon={(props.iconCancel) ? props.iconCancel : "pi pi-times"} onClick={props.onHide} className="p-button-secondary" />
        </div>
    );

    return(
        <Dialog header={props.header} visible={props.visible} style={(props.dialogStyle) ? props.dialogStyle : {width: '50vw'}} footer={footer} onHide={props.onHide} maximizable={props.maximizable}>                        
            {props.children}
        </Dialog>
    );
}

export default ConfirmDialog;
