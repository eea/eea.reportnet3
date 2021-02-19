import React, { useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';

import { PublicLayout } from 'ui/views/_components/Layout/PublicLayout';
import { Title } from 'ui/views/_components/Title';

import { DataflowService } from 'core/services/Dataflow';

export const PublicDataflowInformation = withRouter(({ history, match }) => {
  const { params } = match;

  const [dataflowData, setDataflowData] = useState({});

  useEffect(() => {
    onLoadDataflowData();
  }, []);

  const onLoadDataflowData = async () => {
    try {
      setDataflowData(await DataflowService.reporting(params.dataflowId));
    } catch (error) {}
  };

  return (
    <PublicLayout>
      <Title
        icon={'clone'}
        iconSize={'4rem'}
        subtitle={'Here should be a very long description'}
        title={'Cosas de dataflowss'}
      />
    </PublicLayout>
  );
});
