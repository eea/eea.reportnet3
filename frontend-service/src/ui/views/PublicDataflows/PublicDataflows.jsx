import React, { useContext, useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';

import dayjs from 'dayjs';
import isEmpty from 'lodash/isEmpty';

import { config } from 'conf';
import { routes } from 'ui/routes';

import styles from './PublicDataflows.module.scss';

import { PublicCard } from 'ui/views/_components/PublicCard';
import { PublicLayout } from 'ui/views/_components/Layout/PublicLayout';

import { ThemeContext } from 'ui/views/_functions/Contexts/ThemeContext';

import { DataflowService } from 'core/services/Dataflow';

import { getUrl } from 'core/infrastructure/CoreUtils';

export const PublicDataflows = withRouter(({ history, match }) => {
  const themeContext = useContext(ThemeContext);

  const [contentStyles, setContentStyles] = useState({});
  const [publicDataflows, setPublicDataflows] = useState([]);

  useEffect(() => {
    onLoadPublicDataflows();
  }, []);

  useEffect(() => {
    if (!themeContext.headerCollapse) {
      setContentStyles({ marginTop: `${config.theme.cookieConsentHeight + 6}px` });
    } else {
      setContentStyles({});
    }
  }, [themeContext.headerCollapse]);

  const onLoadPublicDataflows = async () => {
    const publicData = await DataflowService.publicData();
    setPublicDataflows(publicData);
  };

  const onOpenDataflow = dataflowId => history.push(getUrl(routes.PUBLIC_DATAFLOW_INFORMATION, { dataflowId: 77 }));

  console.log('publicDataflows', publicDataflows);

  return (
    <PublicLayout>
      <div style={contentStyles}>
        <h3>Public dataflows:</h3>
        <div className={styles.dataflowsList}>
          {!isEmpty(publicDataflows) ? (
            publicDataflows.map(dataflow => (
              <PublicCard
                key={dataflow.id}
                title={{ text: dataflow.name, url: '' }}
                subtitle={{ text: dataflow.description, url: '' }}
                card={dataflow}
                dueDate={dataflow.deadlineDate > 0 ? dayjs(dataflow.deadlineDate * 1000).format('YYYY-MM-DD') : '-'}
                onCardClick={onOpenDataflow}
              />
            ))
          ) : (
            <span>No public dataflows available</span>
          )}
        </div>
      </div>
    </PublicLayout>
  );
});
