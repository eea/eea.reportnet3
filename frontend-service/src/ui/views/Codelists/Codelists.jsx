import React, { useEffect, useContext, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { CodelistsManager } from 'ui/views/_components/CodelistsManager';
import { MainLayout } from 'ui/views/_components/Layout';
import { Title } from 'ui/views/_components/Title';

import { BreadCrumbContext } from 'ui/views/_functions/Contexts/BreadCrumbContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';

const Codelists = withRouter(({ match, history, isCustodian = false }) => {
  const breadCrumbContext = useContext(BreadCrumbContext);
  const resources = useContext(ResourcesContext);

  useEffect(() => {
    breadCrumbContext.add([
      {
        label: resources.messages['dataflowList'],
        icon: 'home',
        href: getUrl(routes.DATAFLOWS),
        command: () => history.push(getUrl(routes.DATAFLOWS))
      },
      { label: resources.messages['codelists'], icon: 'list' }
    ]);
  }, []);

  const layout = children => {
    return (
      <MainLayout>
        <div className="rep-container" style={{ paddingBottom: '80px' }}>
          {children}
        </div>
      </MainLayout>
    );
  };

  return layout(
    <React.Fragment>
      <Title title={`${resources.messages['codelists']} `} icon="list" iconSize="3.5rem" />
      <CodelistsManager />
    </React.Fragment>
  );
});

export { Codelists };
