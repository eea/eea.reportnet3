import React from 'react';
import styles from './Title.module.css';
import dsIcon from '../../../assets/images/dataset_icon.png';

const Title = (props) => {
    return (      
      <div className={styles.Title}>
        <h2><img  src={dsIcon} alt="Dataset"/>{props.title}</h2>
      </div>  
    );
}

export default React.memo(Title);