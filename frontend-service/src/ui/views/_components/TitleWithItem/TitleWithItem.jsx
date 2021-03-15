import React from 'react';

import uuid from 'uuid';

import pushNotificationInfo from 'assets/images/gifs/pushNotificationInfo.gif';

import { AwesomeIcons } from 'conf/AwesomeIcons';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import ReactTooltip from 'react-tooltip';

import styles from './TitleWithItem.module.scss';

const TitleWithItem = React.memo(
  ({
    hasInfoTooltip = false,
    icon,
    iconSize,
    imgClassName = '',
    items,
    subtitle,
    title,
    tooltipInfo = 'Info tooltip'
  }) => {
    const tooltipId = uuid.v4();

    return (
      <div className={styles.rowContainer}>
        <div className={styles.titleWrap}>
          <div className={styles.iconWrap}>
            <FontAwesomeIcon className={styles.icon} icon={AwesomeIcons(icon)} style={{ fontSize: iconSize }} />
          </div>
          <div className={styles.textWrap}>
            <span className={styles.title}>
              {title}
              {hasInfoTooltip && (
                <>
                  <FontAwesomeIcon
                    aria-hidden={false}
                    className={`${styles.infoButton} p-button-rounded p-button-secondary-transparent`}
                    data-for={tooltipId}
                    data-tip
                    icon={AwesomeIcons('infoCircle')}
                  />

                  <ReactTooltip className={styles.tooltip} effect="solid" id={tooltipId} place="right">
                    {
                      <div className={styles.infoTooltipWrapper}>
                        <span>{tooltipInfo}</span>
                        <img alt={tooltipInfo} className={imgClassName} src={pushNotificationInfo}></img>
                      </div>
                    }
                  </ReactTooltip>
                </>
              )}
            </span>
            <span className={styles.subtitle}>{subtitle}</span>
          </div>
        </div>
        <div className={styles.itemsContainer}>
          {items.map((item, i) => (
            <div className={styles.itemContainer} key={i}>
              {item}
            </div>
          ))}
        </div>
      </div>
    );
  }
);

export { TitleWithItem };
