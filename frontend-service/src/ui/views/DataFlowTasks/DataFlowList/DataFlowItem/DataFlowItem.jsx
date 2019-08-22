import React, { useContext } from 'react';
import { Link } from 'react-router-dom';

import moment from 'moment';

import styles from './DataFlowItem.module.scss';

import primeIcons from 'assets/conf/prime.icons';

import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { IconComponent } from 'ui/views/_components/IconComponent';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

export const DataFlowItem = ({ itemContent, listType, dataFetch }) => {
  const resources = useContext(ResourcesContext);

  const updateStatusDataFlow = type => {
    const dataPromise = HTTPRequester.update({
      url: window.env.REACT_APP_JSON
        ? `/dataflow/updateStatusRequest/${itemContent.id}?type=${type}`
        : `/dataflow/updateStatusRequest/${itemContent.id}?type=${type}`,
      data: { id: itemContent.id },
      queryString: {}
    });

    dataPromise
      .then(response => {
        dataFetch();
      })
      .catch(error => {
        return error;
      });
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
        <IconComponent icon={`${primeIcons.icons.clone}`} className={`${styles.card_component_icon_i}`} />
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
            <button type="button" className={`${styles.rep_button}`} onClick={() => updateStatusDataFlow('ACCEPTED')}>
              {resources.messages['accept']}
            </button>

            <button type="button" className={`${styles.rep_button}`} onClick={() => updateStatusDataFlow('REJECTED')}>
              {resources.messages['reject']}
            </button>
          </>
        ) : (
          <>
            {/* <a className={styles.btn} href="#"> */}
            <IconComponent icon={`${primeIcons.icons.comment}`} />
            {/* </a> */}
            {/* <a className={styles.btn} href="http://"> */}
            <IconComponent icon={`${primeIcons.icons.share}`} />
            {/* </a> */}
          </>
        )}
      </div>
    </>
  );
};
