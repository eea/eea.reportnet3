import React from 'react';
import { Toolbar } from 'primereact/toolbar';
import CustomButton from '../CustomButton/CustomButton';

const ButtonsBar = (props) => {

const leftButtons = [];
const rightButtons = [];

const buttons = props.buttons.forEach((b,i) => {
    (b.group==="left")?
        leftButtons.push(<CustomButton label={b.label} icon={b.icon} key={i} handleClick={b.clickHandler} disabled={b.disabled} ownButtonClasses={b.ownButtonClasses} iconClasses={b.iconClasses}/>)
        :rightButtons.push(<CustomButton label={b.label} icon={b.icon} key={i} handleClick={b.clickHandler} disabled={b.disabled} ownButtonClasses={b.ownButtonClasses} iconClasses={b.iconClasses}/>)
});

    return (
        <Toolbar>    
            {buttons}
            <div className="p-toolbar-group-left">
                {leftButtons}
            </div>
            <div className="p-toolbar-group-right">
                {rightButtons}
            </div>

            {/* <div className="p-toolbar-group-left">
                <CustomButton label="Import" icon="0" />
                <CustomButton label="Export" icon="1" />
                <CustomButton label="Delete" icon="2" />
            </div>
            <div className="p-toolbar-group-right">
                <CustomButton label="Events" icon="4" />
                <CustomButton label="Validations" icon="3" />
                <CustomButton label="Dashboards" icon="5" />
            </div> */}
        </Toolbar>
    );
}

export default ButtonsBar;