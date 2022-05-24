import React from 'react';

import { config } from 'conf';

import styles from './TrafficLight.module.scss';

export const TrafficLight = ({ className, sqlSentenceCost }) => {
  const getColor = cost => {
    if (cost < config.SQL_SENTENCE_LOW_COST) {
      return 'green';
    } else if (cost < config.SQL_SENTENCE_HIGH_COST && cost > config.SQL_SENTENCE_LOW_COST) {
      return 'yellow';
    } else {
      return 'red';
    }
  };

  const color = getColor(sqlSentenceCost);

  return (
    <div className={`${styles.trafficLight} ${className}`}>
      <div className={color === 'green' ? styles.greenLightSignal : ''} key="green"></div>
      <div className={color === 'yellow' ? styles.yellowLightSignal : ''} key="yellow"></div>
      <div className={color === 'red' ? styles.redLightSignal : ''} key="red"></div>
    </div>
  );
};
