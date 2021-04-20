import React, { Fragment, useContext, useEffect, useState } from 'react';
import ReactTooltip from 'react-tooltip';
import { Link } from 'react-router-dom';

import uuid from 'uuid';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import dayjs from 'dayjs';
import isNil from 'lodash/isNil';

import styles from './DataflowsItem.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';

const DataflowsItem = ({ isCustodian, itemContent, reorderDataflows = () => {} }) => {
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [isPinned, setIsPinned] = useState(itemContent.pinned === 'pinned');
  const [isPinning, setIsPinning] = useState(false);
  const [isPinShowed, setIsPinShowed] = useState(false);

  useEffect(() => {
    setIsPinned(itemContent.pinned === 'pinned');
  }, [itemContent, isPinning]);

  const layout = children => {
    return (
      <div
        className={`${styles.container} ${styles.accepted} ${
          styles[itemContent.status]
        } dataflowList-first-dataflow-help-step`}
        onMouseEnter={() => setIsPinShowed(true)}
        onMouseLeave={() => setIsPinShowed(false)}>
        <Link className={`${styles.containerLink}`} to={getUrl(routes.DATAFLOW, { dataflowId: itemContent.id }, true)}>
          {children}
        </Link>
        <div className={`${styles.pinContainer} ${isPinShowed || isPinned ? styles.pinShowed : styles.pinHidden}`}>
          <FontAwesomeIcon
            className={`${isPinned ? styles.pinned : styles.notPinned} ${isPinning ? 'fa-spin' : null}`}
            icon={!isPinning ? AwesomeIcons('pin') : AwesomeIcons('spinner')}
            onClick={async () => {
              setIsPinning(true);
              await reorderDataflows(itemContent, !isPinned);
              setIsPinning(false);
            }}
          />
        </div>
      </div>
    );
  };

  const idTooltip = uuid.v4();

  return layout(
    <Fragment>
      <div className={`${styles.icon}`}>
        <FontAwesomeIcon icon={AwesomeIcons('clone')} />
      </div>

      <div className={`${styles.deliveryDate} dataflowList-delivery-date-help-step`}>
        <p>
          <span>{`${resources.messages['deliveryDate']}: `}</span>
          <span className={`${styles.dateBlock}`}>
            {itemContent.expirationDate == '-'
              ? resources.messages['pending']
              : dayjs(itemContent.expirationDate).format(userContext.userProps.dateFormat)}
          </span>
        </p>
      </div>

      <div className={`${styles.text} dataflowList-name-description-help-step`}>
        <h3 className={`${styles.title}`} data-tip data-for={idTooltip}>
          {itemContent.name}
        </h3>
        <p>{itemContent.description}</p>
        {itemContent.name.length > 70 && (
          <ReactTooltip className={styles.tooltip} effect="solid" id={idTooltip} place="top">
            {itemContent.name}
          </ReactTooltip>
        )}
      </div>

      <div className={`${styles.status}  dataflowList-status-help-step`}>
        {!isCustodian && !isNil(itemContent.reportingDatasetsStatus) && itemContent.status === 'OPEN' && (
          <p>
            <span>{`${resources.messages['deliveryStatus']}: `}</span>
            {itemContent.reportingDatasetsStatus === 'PENDING'
              ? resources.messages['draft'].toUpperCase()
              : itemContent.reportingDatasetsStatus.split('_').join(' ').toUpperCase()}
          </p>
        )}
        <p>
          <span>{`${resources.messages['dataflowStatus']}: `}</span>
          {itemContent.status}
        </p>
      </div>
      <div className={`${styles.role}  dataflowList-role-help-step`}>
        <p>
          <span>{`${resources.messages['role']}: `}</span>
          {itemContent.userRole?.label}
        </p>
      </div>

      <div className={`${styles.obligation} `}>
        <p className="dataflowList-obligation-description-help-step">
          {!isNil(itemContent.legalInstrument) ? (
            <Fragment>
              <span>{`${resources.messages['legalInstrumentDataflowItem']}: `}</span>
              {itemContent.legalInstrument}
            </Fragment>
          ) : null}
        </p>
        <p>
          <span>{`${resources.messages['obligationDataflowItem']}: `}</span>
          {itemContent.obligationTitle}
        </p>
      </div>
    </Fragment>
  );
};

export { DataflowsItem };
