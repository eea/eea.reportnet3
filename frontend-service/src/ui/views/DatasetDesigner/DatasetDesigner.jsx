import React, { useState, useEffect, useContext, useReducer, useRef } from 'react';
import moment from 'moment';
import { withRouter } from 'react-router-dom';
import { isUndefined } from 'lodash';

import styles from './DatasetDesigner.module.scss';

import { config } from 'conf';

import { BreadCrumb } from 'ui/views/_components/BreadCrumb';
import { Button } from 'ui/views/_components/Button';
import { Growl } from 'primereact/growl';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabsDesigner } from './_components/TabsDesigner';

import { Toolbar } from 'ui/views/_components/Toolbar';

import { getUrl } from 'core/infrastructure/api/getUrl';
import { routes } from 'ui/routes';
import { UserContext } from 'ui/views/_components/_context/UserContext';
import { UserService } from 'core/services/User';

export const DatasetDesigner = withRouter(({ match, history }) => {
  const {
    params: { dataflowId, datasetId }
  } = match;
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [hasWritePermissions, setHasWritePermissions] = useState(false);
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);
  const [isLoading, setIsLoading] = useState(false);

  let growlRef = useRef();
  const home = {
    icon: config.icons['home'],
    command: () => history.push(getUrl(routes.DATAFLOWS))
  };

  useEffect(() => {
    if (!isUndefined(user.contextRoles)) {
      setHasWritePermissions(
        UserService.hasPermission(user, [config.permissions.PROVIDER], `${config.permissions.DATASET}${datasetId}`)
      );
    }
  }, [user]);

  useEffect(() => {
    setBreadCrumbItems([
      {
        label: resources.messages['dataflowList'],
        command: () => history.push(getUrl(routes.DATAFLOWS))
      },
      {
        label: resources.messages['dataflow'],
        command: () =>
          history.push(
            getUrl(
              routes.DATAFLOW,
              {
                dataflowId: match.params.dataflowId
              },
              true
            )
          )
      },
      { label: resources.messages['datasetDesigner'] }
    ]);
  }, []);

  const layout = children => {
    return (
      <MainLayout>
        <Growl ref={growlRef} />
        <BreadCrumb model={breadCrumbItems} home={home} />
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  if (isLoading) {
    return layout(<Spinner />);
  }

  return layout(
    <>
      <div className={styles.ButtonsBar}>
        <Toolbar>
          <div className="p-toolbar-group-right">
            <Button
              className={`p-button-rounded p-button-secondary`}
              disabled={true}
              icon={'clock'}
              label={resources.messages['events']}
              onClick={null}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              // disabled={!hasWritePermissions || (Number(datasetId) === 5 || Number(datasetId) === 142)}
              disabled={true}
              icon={'validate'}
              label={resources.messages['validate']}
              onClick={() => null}
              ownButtonClasses={null}
              iconClasses={null}
            />

            <Button
              className={`p-button-rounded p-button-secondary`}
              // disabled={Number(datasetId) === 5 || Number(datasetId) === 142}
              disabled={true}
              icon={'dashboard'}
              label={resources.messages['dashboards']}
              onClick={() => null}
            />
            <Button
              className={`p-button-rounded p-button-secondary`}
              // disabled={!hasWritePermissions}
              disabled={true}
              icon={'camera'}
              label={resources.messages['snapshots']}
              onClick={() =>
                /* 
                onLoadSnapshotList();
                return onSetVisible(setSnapshotIsVisible, true); */
                null
              }
            />
          </div>
        </Toolbar>
      </div>
      <TabsDesigner>
        {/* <Toolbar>
            <div className="p-toolbar-group-left">
              <Button
                className={`p-button-rounded p-button-secondary`}
                disabled={false}
                icon={'export'}
                label={resources.messages['upload']}
                onClick={() => null}
              />
              <Button
                className={`p-button-rounded p-button-secondary`}
                disabled={true}
                icon={'eye'}
                label={resources.messages['visibility']}
                onClick={null}
              />
              <Button
                className={`p-button-rounded p-button-secondary`}
                disabled={true}
                icon={'filter'}
                label={resources.messages['filter']}
                onClick={null}
              />
              <Button
                className={`p-button-rounded p-button-secondary`}
                disabled={true}
                icon={'import'}
                label={resources.messages['export']}
                onClick={null}
              />
            </div>
            <div className="p-toolbar-group-right">
              <Button
                className={`p-button-rounded p-button-secondary`}
                disabled={false}
                icon={'refresh'}
                label={resources.messages['refresh']}
                onClick={() => null}
              />
            </div>
          </Toolbar> */}
        {
          // <DataTable autoLayout={true} paginator={true} rowsPerPageOptions={[5, 10, 100]} rows={10}>
          //   <Column className={styles.crudColumn} />
          //   <Column
          //     columnResizeMode="expand"
          //     field="title"
          //     filter={false}
          //     filterMatchMode="contains"
          //     header={resources.messages['title']}
          //     sortable={true}
          //   />
          //   <Column
          //     field="description"
          //     filter={false}
          //     filterMatchMode="contains"
          //     header={resources.messages['description']}
          //     sortable={true}
          //   />
          //   <Column
          //     field="category"
          //     filter={false}
          //     filterMatchMode="contains"
          //     header={resources.messages['category']}
          //     sortable={true}
          //   />
          //   <Column
          //     field="language"
          //     filter={false}
          //     filterMatchMode="contains"
          //     header={resources.messages['language']}
          //     sortable={true}
          //   />
          //   <Column
          //     /*   body={actionTemplate} */
          //     field="url"
          //     filter={false}
          //     filterMatchMode="contains"
          //     header={resources.messages['file']}
          //     style={{ textAlign: 'center', width: '8em' }}
          //   />
          // </DataTable>
        }
      </TabsDesigner>
    </>
  );
});
