import React, { useContext, useEffect, useState, useReducer } from 'react';

import styles from './UserObligations.module.scss';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { TreeViewExpandableItem } from 'ui/views/_components/TreeView/_components/TreeViewExpandableItem';

import { DataflowService } from 'core/services/Dataflow';
import { Spinner } from 'ui/views/_components/Spinner';
import { apiDataset } from 'core/infrastructure/api/domain/model/Dataset';
import { Dataset } from 'core/domain/model/Dataset/Dataset';
import { isEmpty, isUndefined, uniq } from 'lodash';

const UserObligations = () => {
  const resources = useContext(ResourcesContext);

  // useEffect(() => {
  //   onLoadDataflowData();
  // }, []);

  useEffect(() => {
    dataFetch();
  }, []);

  useEffect(() => {
    schemaById();
  }, []);

  useEffect(() => {
    // onLoadReportingDataflow(match);
  }, []);

  const [loading, setLoading] = useState(true);
  const [acceptedContent, setacceptedContent] = useState([]);
  const [initialvalues, setInitialValues] = useState([]);
  const [designDatasetSchemas, setDesignDatasetSchemas] = useState([]);
  const [dataflowData, setDataflowData] = useState();
  const [dataflowStatus, setDataflowStatus] = useState();
  const [updatedDatasetSchema, setUpdatedDatasetSchema] = useState();

  const onLoadReportingDataflow = async match => {
    try {
      const dataflow = await DataflowService.reporting(match.params.dataflowId);
      setDataflowData(dataflow);
      setDataflowStatus(dataflow.status);
      dataflow.designDatasets.forEach((schema, idx) => {
        schema.index = idx;
      });
      setDesignDatasetSchemas(dataflow.designDatasets);
      const datasetSchemaInfo = [];
      dataflow.designDatasets.map(schema => {
        datasetSchemaInfo.push({ schemaName: schema.datasetSchemaName, schemaIndex: schema.index });
      });
      setUpdatedDatasetSchema(datasetSchemaInfo);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  const dataFetch = async () => {
    setLoading(true);
    try {
      const allDataflows = await DataflowService.all();

      setacceptedContent(allDataflows.accepted);

      const dataflowInitialValues = [];
      allDataflows.accepted.forEach(element => {
        dataflowInitialValues[element.id] = { name: element.name, id: element.id, dataset: element.datasets };
      });
      setInitialValues(dataflowInitialValues);
    } catch (error) {
      console.error('dataFetch error: ', error);
    }
    setLoading(false);
  };

  const schemaById = async datasetId => {
    const datasetSchemaDTO = await apiDataset.schemaById(datasetId);
    const dataset = new Dataset();
    dataset.datasetSchemaDescription = datasetSchemaDTO.description;
    dataset.datasetSchemaId = datasetSchemaDTO.idDataSetSchema;
    dataset.datasetSchemaName = datasetSchemaDTO.nameDatasetSchema;
  };

  const renderChildrenArray = () => {
    if (loading) {
      return <Spinner />;
    }
    return acceptedContent.map(item => (
      <TreeViewExpandableItem
        className={styles.obligationsExpandable}
        expanded={false}
        items={[{ label: item.name }]}
        key={item.id}
        children={['']}>
        {item.datasets
          ? item.datasets.map((dataset, index) => (
              <ul key={index}>
                <li className={styles.children}>{dataset}</li>
              </ul>
            ))
          : null}
      </TreeViewExpandableItem>
    ));
  };

  return (
    <div className={styles.userObligationsContainer}>
      <div className={styles.userObligationsTitle}>{resources.messages['userObligations']}</div>
      {renderChildrenArray()}
    </div>
  );
};

export { UserObligations };
