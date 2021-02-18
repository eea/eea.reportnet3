import React, { Fragment, useContext } from 'react';
import PropTypes from 'prop-types';

import uuid from 'uuid';

import styles from './PublicCard.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import ReactTooltip from 'react-tooltip';

export const PublicCard = ({
  animation,
  dataflowId,
  dueDate,
  instrument,
  obligation,
  onCardClick,
  isReleasable,
  subtitle,
  title
}) => {
  const idTooltip = uuid.v4();

  const renderRedirectText = (text, url) => (
    <a href={url} target="_blank" title={text}>
      <span>
        {text} <FontAwesomeIcon aria-hidden={false} className="p-breadcrumb-home" icon={AwesomeIcons('externalLink')} />
      </span>
    </a>
  );

  return (
    <div
      className={`${styles.card} ${animation ? styles.clickable : undefined}`}
      onClick={() => onCardClick(dataflowId)}>
      <div className={styles.content}>
        <div className={styles.text}>
          <h3 className={styles.title} className={styles.link} title={title.text}>
            {title.text}
            <FontAwesomeIcon
              aria-hidden={false}
              className={`${styles.link} p-breadcrumb-home`}
              icon={AwesomeIcons('externalLink')}
            />
          </h3>
          <h4 className={styles.subtitle} data-tip data-for={idTooltip}>
            {subtitle.url ? renderRedirectText(subtitle.text, subtitle.url) : subtitle.text}
          </h4>
          <ReactTooltip className={styles.tooltip} effect="solid" id={idTooltip} place="top">
            {subtitle.url ? renderRedirectText(subtitle.text, subtitle.url) : subtitle.text}
          </ReactTooltip>
        </div>
        {obligation && (
          <div className={styles.legalInstrumentAndObligation}>
            <p onClick={e => e.stopPropagation()}>
              <strong>Obligation: </strong>
              {renderRedirectText(
                obligation.title,
                `https://rod.eionet.europa.eu/obligations/${obligation.obligationId}`
              )}
            </p>
          </div>
        )}
        {instrument && (
          <div className={styles.legalInstrumentAndObligation}>
            <p onClick={e => e.stopPropagation()}>
              <strong>Instrument: </strong>
              {renderRedirectText(
                obligation.legalInstruments.alias,
                `https://rod.eionet.europa.eu/instruments/${obligation.legalInstruments.id}`
              )}
            </p>
          </div>
        )}

        <div className={`${styles.footer}`}>
          <span>
            {
              <Fragment>
                <strong>Status: </strong>
                {`${isReleasable ? 'OPEN' : 'CLOSED'}`}
              </Fragment>
            }
          </span>
          <span>
            <strong>Delivery date:</strong> {dueDate}
          </span>
        </div>
      </div>
    </div>
  );
};

PublicCard.propTypes = {
  animation: PropTypes.bool,
  onCardClick: PropTypes.func,
  subtitle: PropTypes.object,
  title: PropTypes.object
};

PublicCard.defaultProps = {
  animation: false,
  onCardClick: () => {},
  subtitle: { text: '', url: '' },
  title: { text: '', url: '' }
};
