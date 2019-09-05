import React, { useContext } from 'react';

import moment from 'moment';

import styles from './DataFlowItem.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Icon } from 'ui/views/_components/Icon';
import { Link } from 'react-router-dom';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

import { DataFlowService } from 'core/services/DataFlow';

export const DataFlowItem = ({ itemContent, listType, dataFetch }) => {
  const resources = useContext(ResourcesContext);

  const onAccept = async () => {
    try {
      const status = await DataFlowService.accept(itemContent.requestId);
      if (status >= 200 && status <= 299) {
        dataFetch();
      } else {
        console.error('AcceptDataFlow error with status: ', status);
      }
    } catch (error) {
      console.error('AcceptDataFlow error: ', error);
    }
  };
  const onReject = async () => {
    try {
      const status = await DataFlowService.reject(itemContent.requestId);
      if (status >= 200 && status <= 299) {
        dataFetch();
      } else {
        console.error('RejectDataFlow error with status: ', status);
      }
    } catch (error) {
      console.error('RejectDataFlow error: ', error);
    }
  };

  const layout = children => {
    return (
      <div
        className={
          listType === 'accepted' || listType === 'completed'
            ? `${styles.container} ${styles.accepted}`
            : `${styles.container}`
        }>
        {listType === 'accepted' ? (
          <Link className={styles.containerLink} to={`/reporting-data-flow/${itemContent.id}`}>
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
      <div className={`${styles.card_component_icon}`}>
        <Icon icon="clone" className={`${styles.card_component_icon_i}`} />
      </div>

      <div className={`${styles.card_component_content} `}>
        <div className={`${styles.card_component_content_date}`}>
          <span>{moment(itemContent.deadlineDate).format('YYYY-MM-DD')}</span>
        </div>
        <h3 className={`${styles.card_component_content_title}`}>{itemContent.name}</h3>

        <p>{itemContent.description}</p>
      </div>

      <div className={`${styles.card_component_btn}`}>
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
            {/* <a className={styles.btn} href="#"> */}
            <Icon icon="comment" />
            {/* </a> */}
            {/* <a className={styles.btn} href="http://"> */}
            <Icon icon="share" />
            {/* </a> */}
          </>
        )}
      </div>
    </>
  );
};
