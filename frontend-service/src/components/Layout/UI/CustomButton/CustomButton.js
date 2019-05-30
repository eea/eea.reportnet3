import React from 'react';
import { Button } from 'primereact/button';
// import styles from './CustomButton.module.css';

const CustomButton = (props) => {
    let icons = [
        'pi pi-upload',
        'pi pi-download',
        'pi pi-trash',
        'pi pi-exclamation-triangle',
        'pi pi-clock',
        'pi pi-chart-bar',
        'pi pi-eye',
        'pi pi-filter',
        'pi pi-sitemap',
        'pi pi-sort',
        'pi pi-check-circle'];

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