import React from 'react';
import { withRouter } from 'react-router-dom';

import { MainLayout } from 'ui/views/_components/Layout';

import { useBreadCrumbs } from 'ui/views/_functions/Hooks/useBreadCrumbs';

import { CurrentPage } from 'ui/views/_functions/Utils';

const TestDatasets = withRouter(({ match, history }) => {
  const {
    params: { dataflowId }
  } = match;

  useBreadCrumbs({ currentPage: CurrentPage.TEST_DATASETS, dataflowId, history });

  const layout = children => {
    return (
      <MainLayout>
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };
  return layout(<div></div>);
});

export { TestDatasets };
