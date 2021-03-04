import React, { useContext, useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { config } from 'conf';
import { routes } from 'ui/routes';

import styles from './PublicDataflows.module.scss';

import { PublicCard } from 'ui/views/_components/PublicCard';
import { Spinner } from 'ui/views/_components/Spinner';
import { PublicLayout } from 'ui/views/_components/Layout/PublicLayout';

import { ThemeContext } from 'ui/views/_functions/Contexts/ThemeContext';

import { DataflowService } from 'core/services/Dataflow';

import { useBreadCrumbs } from 'ui/views/_functions/Hooks/useBreadCrumbs';

import { CurrentPage } from 'ui/views/_functions/Utils';
import { getUrl } from 'core/infrastructure/CoreUtils';

export const PublicDataflows = withRouter(({ history, match }) => {
  const themeContext = useContext(ThemeContext);

  const [contentStyles, setContentStyles] = useState({});
  const [isLoading, setIsLoading] = useState(true);
  const [publicDataflows, setPublicDataflows] = useState([]);

  useBreadCrumbs({ currentPage: CurrentPage.PUBLIC_DATAFLOWS, history });

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
      setPublicDataflows(publicData.data);
    } catch (error) {
      console.error('error', error);
    } finally {
      setIsLoading(false);
    }
  };

  const onOpenDataflow = dataflowId => {
    return history.push(getUrl(routes.PUBLIC_DATAFLOW_INFORMATION, { dataflowId }, true));
  };

  return (
    <PublicLayout>
      <div className={styles.content} style={contentStyles}>
        <div className={`rep-container ${styles.repContainer}`}>
          <h1 className={styles.title}>Dataflows</h1>
          <div className={styles.dataflowsList}>
            {!isLoading ? (
              publicDataflows.map(dataflow => (
                <PublicCard
                  animation
                  card={dataflow}
                  dueDate={dataflow.expirationDate}
                  instrument={dataflow.obligation.legalInstruments}
                  isReleasable={dataflow.isReleasable}
                  key={dataflow.id}
                  obligation={dataflow.obligation}
                  onCardClick={() => onOpenDataflow(dataflow.id)}
                  subtitle={{ text: dataflow.description, url: '' }}
                  title={{ text: dataflow.name, url: '' }}
                />
              ))
            ) : (
              <Spinner style={{ top: 0, left: 0 }} />
            )}
          </div>
        </div>
      </div>
    </PublicLayout>
  );
});
