import React from 'react';
import UserCard from '../Layout/UI/UserCard/UserCard';
import logo from '../../assets/images/logo.png';
import styles from './Navigation.module.css';
import messages from '../../conf/messages.en.json';

const Navigation = () => {
    return (           
      <div id="header" className={styles.header}>
        <div className={styles.headerLeft}>
          <h1><a href="#home" className="appLogo" title={messages.titleHeader}>
              <img height="50px" src={logo} alt="Reportnet" className={styles.appLogo}>
              </img>{messages.titleHeader}</a>
          </h1>
        </div>
        <div className={styles.headerRight}>
          <UserCard/>
        </div>
      </div>     
    );
}

export default Navigation;