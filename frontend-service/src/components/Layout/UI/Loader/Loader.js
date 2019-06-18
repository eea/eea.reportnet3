import React from 'react';
import PropTypes from 'prop-types';
import styles from './Loader.module.css';
import logoSpinner from '../../../../assets/images/logo_spinner.png';

export const Loader = React.memo((props) =>
(
    // <div className={styles.LoaderDiv}>
    //     {props.loadingMessage}
    //     <img src={logoSpinner} className={styles.Loader} alt=""></img>
    // </div>
        <div className={styles.ProgressBar}>
            <span className={styles.Bar}>
            {/* <img src={logoSpinner} className={styles.Loader} alt=""></img> */}
                <span className={styles.Progress}></span>
            </span>
        </div>
));

Loader.propTypes = {
    loadingMessage: PropTypes.string
};
