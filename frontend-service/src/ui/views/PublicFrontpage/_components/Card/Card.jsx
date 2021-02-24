import React, { useContext, Fragment } from 'react';

import styles from './Card.module.scss';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const Card = card => {
  const resources = useContext(ResourcesContext);
  return (
    <div className={`${styles.card} ${styles.public}`}>
      <div className={styles.content}>
        <div className={styles.text}>
          <h3 className={styles.title} title={card.dataflow}>
            <a href={card.dataFlowUrl} target="_blank" rel="noopener noreferrer" title={card.dataflow}>
              <span>
                {card.dataflow}{' '}
                <FontAwesomeIcon
                  aria-hidden={false}
                  className="p-breadcrumb-home"
                  icon={AwesomeIcons('externalLink')}
                />
              </span>
            </a>
          </h3>
          <h4 className={styles.subtitle} title={card.legalInstrument}>
            {card.legalInstrumentUrl ? (
              <a href={card.legalInstrumentUrl} target="_blank" rel="noopener noreferrer">
                <span>
                  {card.legalInstrument}{' '}
                  <FontAwesomeIcon
                    aria-hidden={false}
                    className="p-breadcrumb-home"
                    icon={AwesomeIcons('externalLink')}
                  />
                </span>
              </a>
            ) : (
              <Fragment>{card.legalInstrument}</Fragment>
            )}
          </h4>
        </div>
        <div className={styles.pilotScenarioAmbition}>
          <p>
            <strong>Pilot scenario ambition: </strong>
            {card.pilotScenarioAmbition}
          </p>
        </div>

        <div className={`${styles.footer}`}>
          <span>
            <strong>Frequency:</strong> {card.reportingFrequency}
          </span>
          <span>
            <strong>Delivery date:</strong> {card.targetDate}
          </span>
        </div>
      </div>
    </div>
  );
};
