import React, { useContext, useEffect, useState } from 'react';

import { capitalize, isEmpty, isUndefined } from 'lodash';

import styles from './TabsValidations.module.css';

import { ActionsColumn } from 'ui/views/_components/ActionsColumn';
import { Button } from 'ui/views/_components/Button';
import { Column } from 'primereact/column';
import { DataTable } from 'ui/views/_components/DataTable';
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
  const resources = useContext(ResourcesContext);

  const [isLoading, setIsLoading] = useState(true);
  const [validations, setValidations] = useState([]);

  useEffect(() => {
    onLoadValidationsList();
  }, []);
  // }, [isValidationDeleted]);

  const onLoadValidationsList = async () => {
    try {
      const validationsList = await ValidationService.getAll(datasetSchemaId);
      setValidations(validationsList);
    } catch (error) {
      console.log({ error });
      notificationContext.add({
        type: 'VALIDATION_SERVICE_GET_ALL_ERROR',
        content: {
          datasetSchemaId
        }
      });
    } finally {
      // setIsLoading(false);
      return;
    }
  };

  const setActionButtons = validations => {
    validations.rules.forEach(validation => {
      validation.actionButtons = (
        <div>
          <Button
            type="button"
            icon="edit"
            className={`p-button-rounded p-button-secondary`}
            // disabled={isDeletingDocument && rowData.id === documentInitialValues.id}
            // onClick={e => onEditDocument()}
          />
          <Button
            type="button"
            icon="trash"
            // icon={isDeletingDocument && rowData.id === documentInitialValues.id ? 'spinnerAnimate' : 'trash'}
            className={`p-button-rounded p-button-secondary`}
            // disabled={isDeletingDocument && rowData.id === documentInitialValues.id}
            // onClick={() => {
            //   setDeleteDialogVisible(true);
            //   setRowDataState(rowData);
            // }}
          />
        </div>
      );
    });
  };

  const validationList = () => {
    console.log({ validations });
    if (isEmpty(validations)) {
      return;
    }

    setActionButtons(validations);

    const headers = [
      {
        id: 'name',
        header: resources.messages['ruleName']
      },
      // {
      //   id: 'shortCode',
      //   header: resources.messages['ruleShortCode']
      // },
      {
        id: 'ruleDescription',
        header: resources.messages['ruleDescription']
      },
      {
        id: 'levelError',
        header: resources.messages['ruleLevelError']
      },
      {
        id: 'enabled',
        header: resources.messages['ruleEnabled']
      },
      {
        id: 'automatic',
        header: resources.messages['ruleAutomatic']
      },
      {
        id: 'actionButtons',
        header: resources.messages['ruleActions']
      }
    ];
    let columnsArray = headers.map(col => <Column sortable={false} key={col.id} field={col.id} header={col.header} />);
    let columns = columnsArray;

    return validations.entityTypes.map(entityType => {
      const validationsFilteredByEntityType = validations.rules.filter(rule => rule.entityType === entityType);
      const paginatorRightText = `${capitalize(entityType)} records: ${validationsFilteredByEntityType.length}`;
      return (
        <div className={null}>
          <DataTable
            autoLayout={true}
            className={null}
            loading={false}
            paginator={true}
            paginatorRight={paginatorRightText}
            rows={10}
            rowsPerPageOptions={[5, 10, 15]}
            totalRecords={validationsFilteredByEntityType.length}
            value={validationsFilteredByEntityType}>
            {columns}
          </DataTable>
        </div>
        // <TabPanel header={entityType} key={entityType} rightIcon={null}>
        //   <div className={null}>
        //     <DataTable
        //       autoLayout={true}
        //       className={null}
        //       loading={false}
        //       paginator={true}
        //       paginatorRight={paginatorRightText}
        //       rows={10}
        //       rowsPerPageOptions={[5, 10, 15]}
        //       totalRecords={validationsFilteredByEntityType.length}
        //       value={validationsFilteredByEntityType}>
        //       {columns}
        //     </DataTable>
        //   </div>
        // </TabPanel>
      );
    });
  };

  if (isLoading) {
    return <Spinner />;
  }

  return (
    // <TabView activeIndex={activeIndex} onTabChange={onTabChange} renderActiveOnly={false}>
    validationList()
    // </TabView>
  );
};

export { TabsValidations };
