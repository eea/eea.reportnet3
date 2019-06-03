import React, { useContext } from 'react';
import { Button } from 'primereact/button';
import ResourcesContext from '../../../Context/ResourcesContext';
// import styles from './CustomButton.module.css';

const CustomButton = (props) => {
    const resources = useContext(ResourcesContext);
    let icons = [
        resources.icons["import"],
        resources.icons["export"],
        resources.icons["trash"],
        resources.icons["warning"],
        resources.icons["clock"],
        resources.icons["dashboard"],
        resources.icons["eye"],
        resources.icons["filter"],
        resources.icons["group-by"],
        resources.icons["sort"],
        resources.icons["validate"],
        resources.icons["refresh"]
    ];

    let disabledButton = props.disabled?true:false;
    //let classes = `p-button-rounded p-button-secondary ${(props.ownButtonClasses)?props.ownButtonClasses:""}`;
    let classes = `p-button-rounded p-button-secondary`;
    let iconClasses = `${icons[props.icon]} ${(props.iconClasses)?props.iconClasses:""}`;

    return (
        /*
        <Tooltip title={props.title}>
            <span>
                <Button className={classes} icon={iconClasses}
                label={props.label} style={{marginRight:'.25em'}} onClick={props.handleClick} disabled={disabledButton}/>
            </span>
        </Tooltip>*/
        <Button className={classes} icon={iconClasses}
                label={props.label} style={{marginRight:'.25em'}} onClick={props.handleClick} disabled={disabledButton}/>
    
                );
}

export default CustomButton;