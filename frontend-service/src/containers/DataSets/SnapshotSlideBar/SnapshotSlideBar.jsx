import React from 'react';
import {Sidebar} from 'primereact/sidebar';

const SnapshotSlideBar = ({isVisible, setIsVisible}) => {
    return (
        <Sidebar visible={isVisible} onHide={(e) => setIsVisible()} position="right" >
            Content
        </Sidebar>
    )
}

export default SnapshotSlideBar;
