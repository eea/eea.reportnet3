import { Fragment, useContext, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import dayjs from 'dayjs';
import isNil from 'lodash/isNil';

import styles from './ReferencedDataflowItem.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';

export const ReferencedDataflowItem = ({ dataflow, reorderDataflows = () => {} }) => {
  const resources = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [isPinned, setIsPinned] = useState(dataflow.pinned === 'pinned');
  const [isPinning, setIsPinning] = useState(false);
  const [isPinShowed, setIsPinShowed] = useState(false);

  useEffect(() => {
    setIsPinned(dataflow.pinned === 'pinned');
  }, [dataflow, isPinning]);

  const renderDataflowLayout = children => (
    <div
      className={`${styles.container} ${styles.accepted} ${
        styles[dataflow.status]
      } dataflowList-first-dataflow-help-step`}
      onMouseEnter={() => setIsPinShowed(true)}
      onMouseLeave={() => setIsPinShowed(false)}>
      <Link
        className={`${styles.containerLink}`}
        to={getUrl(routes.REFERENCE_DATAFLOW, { referenceDataflowId: dataflow.id }, true)}>
        {children}
      </Link>
      <div className={`${styles.pinContainer} ${isPinShowed || isPinned ? styles.pinShowed : styles.pinHidden}`}>
        <FontAwesomeIcon
          className={`${isPinned ? styles.pinned : styles.notPinned} ${isPinning ? 'fa-spin' : null}`}
          icon={!isPinning ? AwesomeIcons('pin') : AwesomeIcons('spinner')}
          onClick={async () => {
            setIsPinning(true);
            await reorderDataflows(dataflow, !isPinned);
            setIsPinning(false);
          }}
        />
      </div>
    </div>
  );

  return renderDataflowLayout(
    <Fragment>
      <div className={`${styles.icon}`}>
        <FontAwesomeIcon icon={AwesomeIcons('clone')} />
      </div>

      <div className={`${styles.deliveryDate} dataflowList-delivery-date-help-step`}>
        <p>
          <span>{`${resources.messages['deliveryDate']}: `}</span>
          <span className={`${styles.dateBlock}`}>
            {dataflow.expirationDate == '-'
              ? resources.messages['pending']
              : dayjs(dataflow.expirationDate).format(userContext.userProps.dateFormat)}
          </span>
        </p>
      </div>

      <div className={`${styles.text} dataflowList-name-description-help-step`}>
        <h3 className={`${styles.title}`}>{dataflow.name}</h3>
        <p>{dataflow.description}</p>
      </div>

      <div className={`${styles.status}  dataflowList-status-help-step`}>
        {!isNil(dataflow.reportingDatasetsStatus) && dataflow.status === 'OPEN' && (
          <p>
            <span>{`${resources.messages['deliveryStatus']}: `}</span>
            {dataflow.reportingDatasetsStatus === 'PENDING'
              ? resources.messages['draft'].toUpperCase()
              : dataflow.reportingDatasetsStatus.split('_').join(' ').toUpperCase()}
          </p>
        )}
        <p>
          <span>{`${resources.messages['dataflowStatus']}: `}</span>
          {dataflow.status}
        </p>
      </div>
      <div className={`${styles.role}  dataflowList-role-help-step`}>
        <p>
          <span>{`${resources.messages['role']}: `}</span>
          {dataflow.userRole}
        </p>
      </div>
    </Fragment>
  );
};
