import React, { useContext, Fragment } from 'react';

import styles from './Card.module.scss';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const Card = card => {
  const resources = useContext(ResourcesContext);
  return (
    <div className={`${styles.card} ${styles.public}`}>
      <div className={styles.content}>
        <div className={styles.text}>
          <h3 className={styles.title} title={card.dataflow}>
            <a
              href={card.dataFlowUrl}
              onClick={e => {
                e.preventDefault();
                window.location.href = card.dataFlowUrl;
              }}
              title={card.dataflow}>
              {card.dataflow}
            </a>
          </h3>
          <h4 className={styles.subtitle} title={card.legalInstrument}>
            {card.legalInstrumentUrl ? (
              <a
                href={card.legalInstrumentUrl}
                onClick={e => {
                  e.preventDefault();
                  window.location.href = card.legalInstrumentUrl;
                }}>
                {card.legalInstrument}
              </a>
            ) : (
              <Fragment>{card.legalInstrument}</Fragment>
            )}
          </h4>
        </div>
        <div className={styles.pilotScenarioAmbition}>
          <p>{card.pilotScenarioAmbition}</p>
        </div>

        <div className={`${styles.footer}`}>
          <span>{card.reportingFrequency}</span>
          <span>{card.targetDate}</span>
        </div>
      </div>
    </div>
  );
};
