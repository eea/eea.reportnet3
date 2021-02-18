import React, { useContext, useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';

import dayjs from 'dayjs';
import isEmpty from 'lodash/isEmpty';

import { config } from 'conf';
import { routes } from 'ui/routes';

import styles from './PublicDataflows.module.scss';

import { PublicCard } from 'ui/views/_components/PublicCard';
import { Spinner } from 'ui/views/_components/Spinner';
import { PublicLayout } from 'ui/views/_components/Layout/PublicLayout';

import { ThemeContext } from 'ui/views/_functions/Contexts/ThemeContext';

import { DataflowService } from 'core/services/Dataflow';

import { getUrl } from 'core/infrastructure/CoreUtils';

export const PublicDataflows = withRouter(({ history, match }) => {
  const themeContext = useContext(ThemeContext);

  const [contentStyles, setContentStyles] = useState({});
  const [isLoading, setIsLoading] = useState(true);
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
    try {
      const publicData = await DataflowService.publicData();
      console.log({ publicData });
      setPublicDataflows(publicData);
    } catch (error) {
      console.error('error', error);
    } finally {
      setIsLoading(false);
    }
  };

  const onOpenDataflow = dataflowId =>
    history.push(getUrl(routes.PUBLIC_DATAFLOW_INFORMATION, { dataflowId: 77 }, true));

  // if (isLoading) return <Spinner />;

  return (
    <PublicLayout>
      <div className={styles.wrap} style={contentStyles}>
        <h3 className={styles.title}>Public dataflows:</h3>
        <div className={styles.dataflowsList}>
          {!isLoading ? (
            publicDataflows.map(dataflow => (
              <PublicCard
                animation
                card={dataflow}
                dueDate={dataflow.deadlineDate > 0 ? dayjs(dataflow.deadlineDate * 1000).format('YYYY-MM-DD') : '-'}
                key={dataflow.id}
                onCardClick={onOpenDataflow}
                subtitle={{ text: dataflow.description, url: '' }}
                title={{ text: dataflow.name, url: '' }}
              />
            ))
          ) : (
            <Spinner style={{ top: 0, left: 0 }} />
          )}
        </div>
      </div>
    </PublicLayout>
  );
});
