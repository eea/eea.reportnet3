import { Fragment, useContext, useEffect, useState } from 'react';
import ReactTooltip from 'react-tooltip';
import { Link } from 'react-router-dom';

import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import dayjs from 'dayjs';
import isNil from 'lodash/isNil';
import uniqueId from 'lodash/uniqueId';

import { config } from 'conf';

import styles from './DataflowsItem.module.scss';

import { AwesomeIcons } from 'conf/AwesomeIcons';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { getUrl } from 'repositories/_utils/UrlUtils';
import { TextUtils } from 'repositories/_utils/TextUtils';
import { routes } from 'conf/routes';

export const DataflowsItem = ({ isAdmin, isCustodian, itemContent, reorderDataflows = () => {} }) => {
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [isPinned, setIsPinned] = useState(itemContent.pinned === 'pinned');
  const [isPinning, setIsPinning] = useState(false);
  const [isPinShowed, setIsPinShowed] = useState(false);

  useEffect(() => {
    setIsPinned(itemContent.pinned === 'pinned');
  }, [itemContent, isPinning]);

  const renderShowPublicInfo = () => {
    const id = uniqueId(itemContent.showPublicInfo ? 'showPublicInfo' : 'doNotShowPublicInfo');
    if ((isCustodian || isAdmin) && itemContent.statusKey === config.dataflowStatus.OPEN) {
      return (
        <div className={`${styles.upperIcon}`}>
          <div>
            <span data-for={id} data-tip>
              <FontAwesomeIcon
                icon={AwesomeIcons(itemContent.showPublicInfo ? 'eye' : 'eyeSlash')}
                role="presentation"
                style={{ opacity: '0.6' }}
              />
            </span>
            <ReactTooltip border={true} className={styles.tooltip} effect="solid" id={id} place="top">
              {resourcesContext.messages[itemContent.showPublicInfo ? 'public' : 'notPublic']}
            </ReactTooltip>
          </div>
        </div>
      );
    } else {
      return null;
    }
  };

  const layout = children => (
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
          alt={resourcesContext.messages['pinDataflow']}
          aria-label={itemContent.pinned}
          className={`${isPinned ? styles.pinned : styles.notPinned} ${isPinning ? 'fa-spin' : null}`}
          icon={!isPinning ? AwesomeIcons('pin') : AwesomeIcons('spinner')}
          onClick={async () => {
            setIsPinning(true);
            await reorderDataflows(itemContent, !isPinned);
            setIsPinning(false);
          }}
          role="presentation"
        />
      </div>
    </div>
  );

  const renderReportingDatasetsStatus = () => {
    const renderDeliveryStatus = () => {
      if (itemContent.reportingDatasetsStatus === 'PENDING') {
        return resourcesContext.messages['draft'].toUpperCase();
      } else {
        return resourcesContext.messages[config.datasetStatus[itemContent.reportingDatasetsStatus].label].toUpperCase();
      }
    };

    if (
      !isCustodian &&
      !isNil(itemContent.reportingDatasetsStatus) &&
      itemContent.statusKey === config.dataflowStatus.OPEN
    ) {
      return (
        <p>
          <span>{`${resourcesContext.messages['deliveryStatus']}: `}</span>
          {renderDeliveryStatus()}
        </p>
      );
    }
  };

  const renderCreationDate = () => {
    if (isCustodian || isAdmin) {
      return (
        <div>
          <span>{`${resourcesContext.messages['creationDate']}: `}</span>
          <span className={`${styles.dateBlock}`}>
            {dayjs(itemContent.creationDate).format(userContext.userProps.dateFormat)}
          </span>
        </div>
      );
    }
  };

  const renderTooltipDescription = () => {
    if (itemContent.name.length > 70) {
      return (
        <ReactTooltip border={true} className={styles.tooltip} effect="solid" id={idTooltip} place="top">
          {itemContent.name}
        </ReactTooltip>
      );
    }
  };

  const renderDeliveryDate = () => {
    if (TextUtils.areEquals(itemContent.expirationDate, '-')) {
      return resourcesContext.messages['pending'];
    } else {
      return dayjs(itemContent.expirationDate).format(userContext.userProps.dateFormat);
    }
  };

  const idTooltip = uniqueId();

  return layout(
    <Fragment>
      {renderShowPublicInfo()}
      <div className={`${styles.icon}`}>
        <FontAwesomeIcon icon={AwesomeIcons('clone')} role="presentation" />
      </div>

      <div className={`${styles.dataflowDates}`}>
        <div>
          {renderCreationDate()}
          <div>
            <span>{`${resourcesContext.messages['deliveryDate']}: `}</span>
            <span className={`${styles.dateBlock}`}>{renderDeliveryDate()}</span>
          </div>
        </div>
      </div>

      <div className={`${styles.text}`}>
        <h3 className={`${styles.title}`} data-for={idTooltip} data-tip>
          {itemContent.name}
        </h3>
        <p>{itemContent.description}</p>
        {renderTooltipDescription()}
      </div>

      <div className={`${styles.status} dataflowList-status-help-step`}>
        {renderReportingDatasetsStatus()}
        <p>
          <span>{`${resourcesContext.messages['dataflowStatus']}: `}</span>
          {itemContent.status}
        </p>
      </div>
      <div className={`${styles.role}  dataflowList-role-help-step`}>
        <p>
          <span>{`${resourcesContext.messages['role']}: `}</span>
          {itemContent.userRole}
        </p>
      </div>

      <div className={`${styles.obligation} `}>
        <p className="dataflowList-obligation-description-help-step">
          <span>{`${resourcesContext.messages['legalInstrument']}: `}</span>
          {itemContent.legalInstrument}
        </p>
        <p>
          <span>{`${resourcesContext.messages['obligation']}: `}</span>
          {itemContent.obligationTitle}
        </p>
      </div>
    </Fragment>
  );
};
