import React from 'react';
import styles from './Title.module.css';

const Title = (props) => {
    return (
      <div className={styles.Title}>
        <h1>{props.title}</h1>
      </div>  
    );
}

export default Title;