import { useContext, useEffect, useState } from 'react';
import { useHistory } from 'react-router';

import { config } from 'conf';
import { routes } from 'conf/routes';

import styles from './PublicDataflows.module.scss';

import { PublicCard } from 'views/_components/PublicCard';
import { Spinner } from 'views/_components/Spinner';
import { PublicLayout } from 'views/_components/Layout/PublicLayout';

import { ThemeContext } from 'views/_functions/Contexts/ThemeContext';

import { DataflowService } from 'services/DataflowService';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

import { useBreadCrumbs } from 'views/_functions/Hooks/useBreadCrumbs';

import { CurrentPage } from 'views/_functions/Utils';
import { getUrl } from 'repositories/_utils/UrlUtils';

export const PublicDataflows = () => {
  const history = useHistory();

  const resourcesContext = useContext(ResourcesContext);
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
      const publicData = await DataflowService.getPublicData();
      setPublicDataflows(publicData);
    } catch (error) {
      console.error('PublicDataflows - onLoadPublicDataflows.', error);
    } finally {
      setIsLoading(false);
    }
  };

  const onOpenDataflow = (dataflowId, newTab) => {
    const url = getUrl(routes.PUBLIC_DATAFLOW_INFORMATION, { dataflowId }, true);
    if (!newTab) {
      return history.push(url);
    } else {
      window.open(url, '_blank');
    }
  };

  return (
    <PublicLayout>
      <div className={styles.content} style={contentStyles}>
        <div className={`rep-container ${styles.repContainer}`}>
          <h1 className={styles.title}>Dataflows</h1>
          <div className={styles.dataflowsList}>
            {!isLoading ? (
              publicDataflows.length !== 0 ? (
                publicDataflows.map(dataflow => (
                  <PublicCard
                    animation
                    card={dataflow}
                    dataflowId={dataflow.id}
                    dueDate={dataflow.expirationDate}
                    key={dataflow.id}
                    landingPageCard={false}
                    obligation={dataflow.obligation}
                    onCardClick={onOpenDataflow}
                    status={resourcesContext.messages[dataflow.status]}
                    subtitle={{ text: dataflow.description, url: '' }}
                    title={{ text: dataflow.name, url: '' }}
                  />
                ))
              ) : (
                <div className={styles.noDataflows}>{resourcesContext.messages['noDataflows']}</div>
              )
            ) : (
              <Spinner style={{ left: 0 }} />
            )}
          </div>
        </div>
      </div>
    </PublicLayout>
  );
};
