import React, { useState, useEffect } from 'react';

import styles from './GoTopButton.module.scss';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

const GoTopButton = ({ scrollMinVisible }) => {

    const [scrollPosition, setScrollPosition] = useState(0)

    const handleScroll = () => {
        const position = window.pageYOffset;
        setScrollPosition(position);
    };

    useEffect(() => {
        window.addEventListener('scroll', handleScroll, { passive: true });
        return () => {
            window.removeEventListener('scroll', handleScroll);
        };

    }, [scrollPosition])

    return (
        scrollPosition > scrollMinVisible &&
        <div className={`${styles.goTopButton}`} onClick={() => window.scrollTo(0, 0)}>
            <FontAwesomeIcon icon={AwesomeIcons('angleUp')} />
        </div>
    )
}

export { GoTopButton };

