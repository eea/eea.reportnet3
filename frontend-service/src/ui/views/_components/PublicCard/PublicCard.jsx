import React, { Fragment, useContext } from 'react';
import PropTypes from 'prop-types';

import uuid from 'uuid';

import styles from './PublicCard.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import ReactTooltip from 'react-tooltip';

export const PublicCard = ({
  card,
  animation,
  dataflowId,
  dueDate,
  instrument,
  obligation,
  onCardClick,
  isReleasable,
  subtitle,
  title,
  externalCard
}) => {
  const idTooltip = uuid.v4();

  const onOpenTab = (e, url) => {
    e.preventDefault();
    window.open(url, '_blank');
    e.stopPropagation();
  };

  const renderRedirectText = (text, url) => (
    <Fragment>
      <span>{text} </span>
      <a href={url} target="_blank" rel="noreferrer" title={text} onClick={e => onOpenTab(e, url)}>
        <FontAwesomeIcon aria-hidden={false} className="p-breadcrumb-home" icon={AwesomeIcons('externalLink')} />
      </a>
    </Fragment>
  );

  const externalCardLayout = children => {
    return (
      <div className={styles.card} onClick={e => onOpenTab(e, card.dataFlowUrl)}>
        {children}
      </div>
    );
  };

  const internalCardLayout = children => {
    return (
      <div className={`${styles.card} ${animation ? styles.clickable : ''}`} onClick={() => onCardClick(dataflowId)}>
        {children}
      </div>
    );
  };

  const getCardBody = () => {
    return (
      <div className={styles.content}>
        <div className={styles.text}>
          <h3 className={`${styles.title} ${styles.link}`} title={title.text}>
            {title.text}
          </h3>
          <h4 className={styles.subtitle} data-tip data-for={idTooltip}>
            {subtitle.url ? renderRedirectText(subtitle.text, subtitle.url) : subtitle.text}
          </h4>
          <ReactTooltip className={styles.tooltip} effect="solid" id={idTooltip} place="top">
            {subtitle.text}
          </ReactTooltip>
        </div>
        {obligation && (
          <div className={styles.legalInstrumentAndObligation}>
            <p>
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
            <p>
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
    );
  };

  if (externalCard) {
    return externalCardLayout(getCardBody());
  }
  return internalCardLayout(getCardBody());
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
