import React from 'react';
import { Button } from 'primereact/button';

const CustomButton = (props) => {
    let icons = [
        'pi pi-upload',
        'pi pi-download',
        'pi pi-trash',
        'pi pi-check-circle',
        'pi pi-clock',
        'pi pi-chart-bar',
        'pi pi-eye',
        'pi pi-filter',
        'pi pi-sitemap',
        'pi pi-sort'];

    return (
        <Button className="p-button-rounded p-button-secondary" icon={icons[props.icon]}
                label={props.label} style={{marginRight:'.25em'}} onClick={props.handleClick} />
    );
}

export default CustomButton;