import { Fragment } from 'react';
import PropTypes from 'prop-types';

import uuid from 'uuid';

import styles from './PublicCard.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import ReactTooltip from 'react-tooltip';

export const PublicCard = ({
  animation,
  card,
  dataflowId,
  dueDate,
  externalCard,
  frequency,
  isReleasable,
  obligation,
  onCardClick,
  pilotScenarioAmbition,
  subtitle,
  title
}) => {
  const idTooltip = uuid.v4();
  const baseRod3Url = 'https://rod.eionet.europa.eu';

  const onOpenTab = (e, url) => {
    e.preventDefault();
    window.open(url, '_blank');
    e.stopPropagation();
  };

  const renderRedirectText = (text, url) => (
    <Fragment>
      <span>{text} </span>
      <a href={url} onClick={e => onOpenTab(e, url)} rel="noreferrer" target="_blank" title={text}>
        <FontAwesomeIcon
          aria-hidden={false}
          aria-label={text}
          className="p-breadcrumb-home"
          icon={AwesomeIcons('externalUrl')}
        />
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
          <h3 className={`${styles.title} ${styles.link}`}>{title.text}</h3>
          <h4 className={styles.subtitle} data-for={idTooltip} data-tip>
            {subtitle.url ? renderRedirectText(subtitle.text, subtitle.url) : subtitle.text}
          </h4>
          <ReactTooltip border={true} className={styles.tooltip} effect="solid" id={idTooltip} place="top">
            {subtitle.text}
          </ReactTooltip>
        </div>

        {externalCard ? (
          <div className={styles.legalInstrumentAndObligation}>
            <p>
              <strong>Pilot scenario ambition: </strong> {pilotScenarioAmbition}
            </p>
          </div>
        ) : (
          <Fragment>
            <div className={styles.legalInstrumentAndObligation}>
              <p>
                <strong>Obligation: </strong>
                {obligation?.obligationId
                  ? renderRedirectText(obligation?.title, `${baseRod3Url}/obligations/${obligation?.obligationId}`)
                  : obligation?.title}
              </p>
            </div>
            <div className={styles.legalInstrumentAndObligation}>
              <p>
                <strong>Instrument: </strong>
                {obligation?.legalInstruments?.id
                  ? renderRedirectText(
                      obligation?.legalInstruments?.alias,
                      `${baseRod3Url}/instruments/${obligation?.legalInstruments?.id}`
                    )
                  : obligation?.legalInstruments?.alias}
              </p>
            </div>
          </Fragment>
        )}
        <div className={`${styles.footer}`}>
          {externalCard ? (
            <span>
              {
                <Fragment>
                  <strong>Frequency: </strong>
                  {frequency}
                </Fragment>
              }
            </span>
          ) : (
            <span>
              {
                <Fragment>
                  <strong>Status: </strong>
                  {`${isReleasable ? 'OPEN' : 'CLOSED'}`}
                </Fragment>
              }
            </span>
          )}
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
