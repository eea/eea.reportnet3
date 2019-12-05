import React, { useContext } from 'react';

import styles from './DataFlowItem.module.scss';

import { routes } from 'ui/routes';

import { Button } from 'ui/views/_components/Button';
import { Link } from 'react-router-dom';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { getUrl } from 'core/infrastructure/api/getUrl';
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import { DataflowService } from 'core/services/DataFlow';

export const DataflowItem = ({ itemContent, listType, dataFetch, position }) => {
  const resources = useContext(ResourcesContext);
  //position must be removed in def implementation
  const statusArray = ['notStarted', 'delivered', 'drafted'];
  let status = 1;
  if (position < 4) {
    status = statusArray[position - 1];
  } else {
    status = statusArray[0];
  }

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
          listType === 'accepted' || listType === 'completed'
            ? `${styles.container} ${styles.accepted} ${styles[status]}`
            : `${styles.container} ${styles[status]}`
        }>
        {listType === 'accepted' ? (
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
      </div>
    );
  };

  return layout(
    <>
      <div className={`${styles.icon}`}>
        <FontAwesomeIcon icon={AwesomeIcons('clone')} />
      </div>

      <div className={`${styles.deliveryDate}`}>
        <span>{resources.messages['deliveryDate']}:</span> {itemContent.deadlineDate}
      </div>

      <div className={styles.text}>
        <h3 className={`${styles.title}`}>{itemContent.name}</h3>

        <p>{itemContent.description}</p>
      </div>
      <div className={styles.status}>
        <p>
          <span>{`${resources.messages['status']}:`}</span> {resources.messages[status]}
        </p>
      </div>

      <div className={`${styles.toolbar}`}>
        {listType === 'pending' ? (
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
        )}
      </div>
    </>
  );
};
