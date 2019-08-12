import React, { useState, useEffect, useContext } from 'react';

import styles from './ReportingDataFlow.module.css';

import { config } from 'assets/conf';

import { DataFlowColumn } from 'ui/views/_components/DataFlowColumn';
import { IconComponent } from 'ui/views/_components/IconComponent';
import { MainLayout } from 'ui/views/_components/Layout';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';

import { BreadCrumb } from 'primereact/breadcrumb';
import { Button } from 'primereact/button';
import { ProgressSpinner } from 'primereact/progressspinner';
import { SplitButton } from 'primereact/splitbutton';

import { HTTPRequester } from 'core/infrastructure/HTTPRequester';
import { getUrl } from 'core/infrastructure/getUrl';

/* import jsonDataSchema from "../../../assets/jsons/datosDataSchema3.json"; */
//import jsonDataSchemaErrors from '../../../assets/jsons/errorsDataSchema.json';

export const ReportingDataFlow = ({ history, match }) => {
  const resources = useContext(ResourcesContext);
  const [breadCrumbItems, setBreadCrumbItems] = useState([]);
  const [dataFlowData, setDataFlowData] = useState(null);
  const [loading, setLoading] = useState(true);

  const home = {
    icon: resources.icons['home'],
    command: () => history.push('/')
  };

  useEffect(() => {
    HTTPRequester.get({
      url: window.env.REACT_APP_JSON
        ? '/jsons/response_DataflowById.json'
        : getUrl(config.loadDataSetsByDataflowID.url, {
            dataFlowId: match.params.dataFlowId
          }),
      queryString: {}
    })
      .then(response => {
        setDataFlowData(response.data);
        setLoading(false);
      })
      .catch(error => {
        setLoading(false);
        console.log('error', error);
        return error;
      });
  }, [match.params.dataFlowId]);

  //Bread Crumbs settings
  useEffect(() => {
    setBreadCrumbItems([
      {
        label: resources.messages['dataFlowTask'],
        command: () => history.push('/data-flow-task')
      },
      {
        label: resources.messages['reportingDataFlow']
      }
    ]);
  }, [history, match.params.dataFlowId, resources.messages]);

  const handleRedirect = target => {
    history.push(target);
  };

  const layout = children => {
    return (
      <MainLayout>
        <BreadCrumb model={breadCrumbItems} home={home} />
        <div className="rep-container">{children}</div>
      </MainLayout>
    );
  };

  if (loading) {
    return layout(<ProgressSpinner />);
  }

  return layout(
    <div className="rep-row">
      <DataFlowColumn
        navTitle={resources.messages['dataFlow']}
        dataFlowTitle={dataFlowData.name}
        buttonTitle={resources.messages['subscribeThisButton']}
      />
      <div className={`${styles.pageContent} rep-col-12 rep-col-sm-9`}>
        <h2 className={styles.title}>
          <IconComponent icon={resources.icons['shoppingCart']} />
          {dataFlowData.name}
        </h2>

        <div className={`${styles.buttonsWrapper}`}>
          <div className={styles.splitButtonWrapper}>
            <div className={`${styles.dataSetItem}`}>
              <Button
                label={resources.messages['do']}
                className="p-button-warning"
                onClick={e => {
                  handleRedirect(`/reporting-data-flow/${match.params.dataFlowId}/documentation-data-set/`);
                }}
              />
              <p className={styles.caption}>{resources.messages['documents']}</p>
            </div>
            {dataFlowData.datasets.map(item => {
              return (
                <div className={`${styles.dataSetItem}`} key={item.id}>
                  <SplitButton
                    label={resources.messages['ds']}
                    model={[
                      {
                        label: resources.messages['releaseDataCollection'],
                        icon: config.icons.archive
                      },
                      {
                        label: resources.messages['importFromFile'],
                        icon: config.icons.import
                      },
                      {
                        label: resources.messages['duplicate'],
                        icon: config.icons.clone
                      },
                      {
                        label: resources.messages['properties'],
                        icon: config.icons.info
                      }
                    ]}
                    onClick={e => {
                      handleRedirect(`/reporting-data-flow/${match.params.dataFlowId}/reporter-data-set/${item.id}`);
                    }}
                  />
                  <p className={styles.caption}>{item.dataSetName}</p>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
};
