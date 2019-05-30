import React from 'react';
import { Dialog } from 'primereact/dialog';
import { Button } from 'primereact/button';

const ConfirmDialog = (props) =>{

    const footer = (
        <div>
            {/* <Button label="Yes" icon="pi pi-check" onClick={props.onClick} />
            <Button label="No" icon="pi pi-times" onClick={onHide} className="p-button-secondary" /> */}
        </div>
    );

    return(
        <div></div>
        // <Dialog header={props.header} visible={props.visible} style={{width: '50vw'}} footer={(props.footer)?props.footer:footer} onHide={onHide} maximizable>                        
        //     {props.children}
        // </Dialog>
    );
}

export default ConfirmDialog;
