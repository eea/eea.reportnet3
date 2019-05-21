import React from 'react';
import styles from './Footer.module.css';

const Footer = () => {
    return (
      <footer className={styles.Footer}>
        <a href=".">About the Commission's new web presence</a>
        <a href=".">Language policy</a>
        <a href=".">Resources for partners</a>
        <a href=".">Cookies</a>
        <a href=".">Privacy policy</a>
        <a href=".">Legal notice</a>
        <a href=".">Contact</a>
      </footer>  
    );
}

export default Footer;