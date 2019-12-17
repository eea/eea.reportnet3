import React, { useContext, useState } from 'react';
import { Link } from 'react-router-dom';

import { isEmpty, isNull, isUndefined } from 'lodash';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { AwesomeIcons } from 'conf/AwesomeIcons';

import styles from './DataflowsItem.module.scss';

import { routes } from 'ui/routes';

import { Button } from 'ui/views/_components/Button';
import { DropdownButton } from 'ui/views/_components/DropdownButton';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { DataflowService } from 'core/services/Dataflow';

export const DataflowsItem = ({
  dataFetch,
  dataflowDispatch,
  dataflowNewValues,
  dataflowId,
  isCustodian,
  itemContent,
  listType,
  position,
  selectedDataflowId,
  showDeleteDialog,
  showEditForm
}) => {
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

  //position must be removed in def implementation
  const statusArray = ['notStarted', 'delivered', 'drafted'];
  let status = 1;
  if (position < 4) {
    status = statusArray[position - 1];
  } else {
    status = statusArray[0];
  }

  const crudMenu = [
    {
      label: resources.messages['edit'],
      icon: 'edit',
      disabled: !isCustodian,
      command: () => {
        showEditForm();
        dataflowDispatch({ type: 'ON_SELECT_DATAFLOW', payload: dataflowId });
      }
    },
    {
      label: resources.messages['delete'],
      icon: 'trash',
      disabled: false,
      command: () => {
        dataflowDispatch({ type: 'ON_SELECT_DATAFLOW', payload: dataflowId });
        showDeleteDialog();
      }
    }
  ];

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

      <div className={styles.crud} onClick={e => e.preventDefault()}>
        <DropdownButton
          icon="ellipsis"
          model={crudMenu}
          buttonStyle={{ display: 'flex', justifyContent: 'flex-end', flexDirection: 'row' }}
        />
      </div>

      <div className={styles.text}>
        <h3 className={`${styles.title}`}>{dataflowTitles.name}</h3>

        <p>{dataflowTitles.description}</p>
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
