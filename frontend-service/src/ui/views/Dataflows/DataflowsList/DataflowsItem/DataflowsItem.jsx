import React, { useContext } from 'react';
import { Link } from 'react-router-dom';

import { isEmpty, isUndefined } from 'lodash';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import styles from './DataflowsItem.module.scss';

import { routes } from 'ui/routes';
import DataflowConf from 'conf/dataflow.config.json';

import { Button } from 'ui/views/_components/Button';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { DataflowService } from 'core/services/Dataflow';

const DataflowsItem = ({ dataFetch, dataflowNewValues, itemContent, selectedDataflowId, type }) => {
  const resources = useContext(ResourcesContext);

  let dataflowTitles = {
    name: itemContent.name,
    description: itemContent.description,
    id: itemContent.id
  };

  if (!isUndefined(selectedDataflowId)) {
    if (dataflowTitles.id === selectedDataflowId && !isEmpty(dataflowNewValues)) {
      dataflowTitles = dataflowNewValues;
    }
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
          type === 'accepted' || type === 'completed'
            ? `${styles.container} ${styles.accepted} ${styles[itemContent.status]}`
            : `${styles.container} ${styles[itemContent.status]}`
        }>
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
      </div>
    );
  };

  return layout(
    <>
      <div className={`${styles.icon}`}>
        <FontAwesomeIcon icon={AwesomeIcons('clone')} />
      </div>

      <div className={`${styles.deliveryDate} dataflowList-delivery-date-help-step`}>
        <span>{resources.messages['deliveryDate']}:</span> {itemContent.deadlineDate}
      </div>

      <div className={`${styles.text} dataflowList-name-description-help-step`}>
        <h3 className={`${styles.title}`}>{dataflowTitles.name}</h3>

        <p>{dataflowTitles.description}</p>
      </div>
      <div className={`${styles.status}  dataflowList-status-help-step`}>
        <p>
          <span>{`${resources.messages['status']}:`}</span> {itemContent.status}
        </p>
      </div>
      <div className={`${styles.role}  dataflowList-role-help-step`}>
        <p>
          <span>{`${resources.messages['role']}:`}</span> {DataflowConf.dataflowRoles[itemContent.userRole]}
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
