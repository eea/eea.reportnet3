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
      setPublicDataflows(publicData);
    } catch (error) {
      console.error('error', error);
    } finally {
      setIsLoading(false);
    }
  };

  const onOpenDataflow = dataflowId => history.push(getUrl(routes.PUBLIC_DATAFLOW_INFORMATION, { dataflowId: 77 }));

  // if (isLoading) return <Spinner />;

  return (
    <PublicLayout>
      <div className={styles.content} style={contentStyles}>
        <div className={`rep-container`}>
          <h3 className={styles.title}>Public dataflows:</h3>
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
      </div>
    </PublicLayout>
  );
});
