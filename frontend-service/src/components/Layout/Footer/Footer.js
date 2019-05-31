import React, {useContext} from 'react';
import styles from './Footer.module.css';
import LangContext from '../../Context/LanguageContext';

const Footer = () => {
  const messages = useContext(LangContext);
    return (
      
      <footer className={styles.Footer}>
        <a href=".">{messages["copyrightAbout"]}</a>
        <a href=".">{messages["copyrightLanguage"]}</a>
        <a href=".">{messages["copyrightResources"]}</a>
        <a href=".">{messages["copyrightCookies"]}</a>
        <a href=".">{messages["copyrightPrivacy"]}</a>
        <a href=".">{messages["copyrightLegal"]}</a>
        <a href=".">{messages["copyrightContact"]}</a>
      </footer>  
    );
}

export default Footer;