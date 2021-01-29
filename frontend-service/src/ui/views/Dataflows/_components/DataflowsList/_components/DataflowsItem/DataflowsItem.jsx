import React, { useContext, useEffect, useState } from 'react';
import ReactTooltip from 'react-tooltip';
import { Link } from 'react-router-dom';

import uuid from 'uuid';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import isNil from 'lodash/isNil';
import dayjs from 'dayjs';

import styles from './DataflowsItem.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import { Button } from 'ui/views/_components/Button';

import { DataflowService } from 'core/services/Dataflow';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

const DataflowsItem = ({ dataFetch, isCustodian, itemContent, reorderDataflows = () => {}, type }) => {
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [isPinned, setIsPinned] = useState(itemContent.pinned === 'pinned');
  const [isPinning, setIsPinning] = useState(false);
  const [isPinShowed, setIsPinShowed] = useState(false);

  useEffect(() => {
    setIsPinned(itemContent.pinned === 'pinned');
  }, [itemContent, isPinning]);

  const onAccept = async () => {
    try {
      const status = await DataflowService.accept(itemContent.requestId);
      if (status >= 200 && status <= 299) {
        dataFetch();
      } else {
        console.error('AcceptDataflow error with status: ', status);
      }
    } catch (error) {
      console.error('AcceptDataflow error: ', error);
    }
  };

  const onReject = async () => {
    try {
      const status = await DataflowService.reject(itemContent.requestId);
      if (status >= 200 && status <= 299) {
        dataFetch();
      } else {
        console.error('RejectDataflow error with status: ', status);
      }
    } catch (error) {
      console.error('RejectDataflow error: ', error);
    }
  };
  const layout = children => {
    return (
      <div
        className={
          type === 'accepted' || type === 'completed'
            ? `${styles.container} ${styles.accepted} ${
                styles[itemContent.status]
              } dataflowList-first-dataflow-help-step`
            : `${styles.container} ${styles[itemContent.status]}`
        }
        onMouseEnter={() => setIsPinShowed(true)}
        onMouseLeave={() => setIsPinShowed(false)}>
        {type === 'accepted' ? (
          <Link
            className={`${styles.containerLink}`}
            to={getUrl(
              routes.DATAFLOW,
              {
                dataflowId: itemContent.id
              },
              true
            )}>
            {children}
          </Link>
        ) : (
          <>{children}</>
        )}
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
    <>
      <div className={`${styles.icon}`}>
        <FontAwesomeIcon icon={AwesomeIcons('clone')} />
      </div>

      <div className={`${styles.deliveryDate} dataflowList-delivery-date-help-step`}>
        <p>
          <>
            <span>{`${resources.messages['deliveryDate']}: `}</span>
            <span className={`${styles.dateBlock}`}>
              {itemContent.expirationDate == '-'
                ? resources.messages['pending']
                : dayjs(itemContent.expirationDate).format(userContext.userProps.dateFormat)}
            </span>
          </>
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
            <span>{`${resources.messages['deliveryStatus']}:`}</span>{' '}
            {itemContent.reportingDatasetsStatus === 'PENDING'
              ? resources.messages['draft'].toUpperCase()
              : itemContent.reportingDatasetsStatus.split('_').join(' ').toUpperCase()}
          </p>
        )}
        <p>
          <span>{`${resources.messages['dataflowStatus']}:`}</span> {itemContent.status}
        </p>
      </div>
      <div className={`${styles.role}  dataflowList-role-help-step`}>
        <p>
          <span>{`${resources.messages['role']}:`}</span> {itemContent.userRole?.replace('_', ' ')}
        </p>
      </div>

      <div className={`${styles.obligation} `}>
        <p className="dataflowList-obligation-description-help-step">
          {!isNil(itemContent.legalInstrument) ? (
            <>
              <span>{`${resources.messages['legalInstrumentDataflowItem']}:`}</span> {itemContent.legalInstrument}
            </>
          ) : null}
        </p>
        <p>
          <>
            <span>{`${resources.messages['obligationDataflowItem']}:`}</span> {itemContent.obligationTitle}
          </>
        </p>
      </div>

      <div className={`${styles.toolbar}`}>
        {type === 'pending' ? (
          <>
            <Button
              layout="simple"
              className={`${styles.rep_button}`}
              onClick={() => onAccept()}
              label={resources.messages['accept']}
            />
            <Button
              className={`${styles.rep_button}`}
              onClick={() => onReject()}
              label={resources.messages['reject']}
            />
          </>
        ) : (
          false && (
            <>
              <span
                className={styles.btn}
                href="#"
                onClick={e => {
                  e.preventDefault();
                }}>
                <FontAwesomeIcon icon={AwesomeIcons('comments')} />
              </span>
              <span
                className={styles.btn}
                href="http://"
                onClick={e => {
                  e.preventDefault();
                }}>
                <FontAwesomeIcon icon={AwesomeIcons('share')} />
              </span>
            </>
          )
        )}
      </div>
    </>
  );
};

export { DataflowsItem };
