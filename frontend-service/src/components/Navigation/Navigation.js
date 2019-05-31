import React, {useContext} from 'react';
import UserCard from '../Layout/UI/UserCard/UserCard';
import logo from '../../assets/images/logo.png';
import styles from './Navigation.module.css';
import LangContext from '../Context/LanguageContext';

const Navigation = () => {
  const messages = useContext(LangContext);
    return (           
      <div id="header" className={styles.header}>
        <div className={styles.headerLeft}>
          <h1><a href="#home" className="appLogo" title={messages["titleHeader"]}>
              <img height="50px" src={logo} alt="Reportnet" className={styles.appLogo}>
              </img>{messages["titleHeader"]}</a>
          </h1>
        </div>
        <div className={styles.headerRight}>
          <UserCard/>
        </div>
      </div>     
    );
}

export default Navigation;