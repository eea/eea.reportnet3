import React, { useContext } from 'react';

import isNil from 'lodash/isNil';

import styles from './PublicCard.module.scss';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

export const PublicCard = ({ card, onCardClick }) => {
  const { legalInstrument, legalInstrumentUrl } = card;

  const resources = useContext(ResourcesContext);

  const renderRedirectText = (text, url) => (
    <a href={url} target="_blank" title={text}>
      <span>
        {text} <FontAwesomeIcon aria-hidden={false} className="p-breadcrumb-home" icon={AwesomeIcons('externalLink')} />
      </span>
    </a>
  );

  return (
    <div className={`${styles.card} ${!isNil(onCardClick) ? styles.clickable : undefined}`}>
      <div className={styles.content}>
        <div className={styles.text}>
          <h3 className={styles.title} title={card.dataflow}>
            {renderRedirectText(card.dataflow, card.dataFlowUrl)}
          </h3>
          <h4 className={styles.subtitle} title={card.legalInstrument}>
            {legalInstrumentUrl ? renderRedirectText(legalInstrument, legalInstrumentUrl) : legalInstrument}
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
