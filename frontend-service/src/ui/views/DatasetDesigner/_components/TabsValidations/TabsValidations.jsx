import React, { useContext, useEffect, useState } from 'react';

import { isEmpty, isUndefined } from 'lodash';

import styles from './TabsValidations.module.css';

import { config } from 'conf';

import { Column } from 'primereact/column';
import { DataViewer } from 'ui/views/_components/DataViewer';
import { DataTable } from 'ui/views/_components/DataTable';
import { MainLayout } from 'ui/views/_components/Layout';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabView } from 'ui/views/_components/TabView';
import { TabPanel } from 'ui/views/_components/TabView/_components/TabPanel';

import { ValidationService } from 'core/services/Validation';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

const TabsValidations = ({
  activeIndex = 0,
  buttonsList = undefined,
  datasetSchemaId,
  hasWritePermissions,
  onTabChange
}) => {
  const notificationContext = useContext(NotificationContext);
  const resourcesContext = useContext(ResourcesContext);

  const [isLoading, setIsLoading] = useState(true);
  const [validations, setValidations] = useState([]);
  const [validationColumns, setValidationColumns] = useState([]);

  useEffect(() => {
    onLoadValidationsList();
  }, []);
  // }, [isValidationDeleted]);

  const getValidationColumns = validationsList => {};

  const onLoadValidationsList = async () => {
    try {
      const validationsList = await ValidationService.getAll(datasetSchemaId);
      setValidations(validationsList);
      setValidationColumns(getValidationColumns(validationsList));
    } catch (error) {
      notificationContext.add({
        type: 'VALIDATION_SERVICE_GET_ALL_VALIDATIONS',
        content: {
          datasetSchemaId
        }
      });
    } finally {
      setIsLoading(false);
    }
  };

  const validationLevelTabs = () => {
    console.log({ validations });
    if (isEmpty(validations)) {
      return;
    }
    console.log(validations);

    const headers = [
      {
        id: 'ruleName',
        header: 'Name'
      },
      {
        id: 'ruleDescription',
        header: 'Description'
      },
      {
        id: 'isRuleAutomatic',
        header: 'Type error'
      },
      {
        id: 'isRuleEnabled',
        header: 'Enabled'
      },
      {
        id: 'isRuleAutomatic',
        header: 'Automatic QC'
      },
      {
        id: 'actionButtons',
        header: 'Edit/delete'
      },
      {
        //Buttons to add, edit, delelet QCs
        id: 'actionButtons',
        header: 'Add'
      }
    ];
    let columnsArray = headers.map(col => <Column sortable={false} key={col.id} field={col.id} header={col.header} />);
    let columns = columnsArray;

    const tabs = [];
    console.log({ validations });
    validations.entityLevels.forEach(entityLevel => {
      const validationsFilteredByEntityLevel = validations.rules.filter(rule => rule.entityType === entityLevel);
      console.log({ validationsFilteredByEntityLevel });
      tabs.push(
        <TabPanel header={entityLevel} key={entityLevel} rightIcon={null}>
          <div className={null}>
            <DataTable
              autoLayout={true}
              className={null}
              loading={false}
              paginator={true}
              paginatorRight={validationsFilteredByEntityLevel.length}
              rows={10}
              rowsPerPageOptions={[5, 10, 15]}
              totalRecords={validationsFilteredByEntityLevel.length}
              value={validationsFilteredByEntityLevel}>
              {columns}
            </DataTable>
          </div>
        </TabPanel>
      );
    });
    return tabs;
  };

  if (isLoading) {
    return <Spinner />;
  }

  return (
    <TabView activeIndex={activeIndex} onTabChange={onTabChange} renderActiveOnly={false}>
      {validationLevelTabs()}
    </TabView>
  );
};

export { TabsValidations };
