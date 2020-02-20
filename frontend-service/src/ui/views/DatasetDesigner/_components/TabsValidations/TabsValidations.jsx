import React, { useContext, useEffect, useState } from 'react';

import { isEmpty, isUndefined } from 'lodash';

import styles from './TabsValidations.module.css';

import { config } from 'conf';

import { DataViewer } from 'ui/views/_components/DataViewer';
import { MainLayout } from 'ui/views/_components/Layout';
import { Spinner } from 'ui/views/_components/Spinner';
import { TabView } from 'ui/views/_components/TabView';
import { TabPanel } from 'ui/views/_components/TabView/_components/TabPanel';

import { ValidationService } from 'core/services/Validation';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';

const TabsValidations = ({
  activeIndex = 0,
  buttonsList = undefined,
  datasetSchemaId,
  hasWritePermissions,
  onTabChange
}) => {
  const notificationContext = useContext(NotificationContext);
  const [isLoading, setIsLoading] = useState(true);
  const [validations, setValidations] = useState([]);

  const getValidations = async () => {
    try {
      return await ValidationService.getAll(datasetSchemaId);
    } catch (error) {
      notificationContext.add({
        type: 'VALIDATION_SERVICE_GET_ALL_VALIDATIONS',
        content: {
          datasetSchemaId
        }
      });
    }
  };

  const validationsList = getValidations();
  console.log(validationsList);

  if (isUndefined(validationsList) || isEmpty(validationsList.rules)) {
    console.log('Schema has no validations');
  }

  let tabs = validationsList.entityLevels;

  if (isLoading) {
    return <Spinner />;
  }
  if (validations) {
    return <></>;
    // let tableHasErrors = true;
    // if (!isUndefined(tables) && !isUndefined(tables[activeIndex])) {
    //   tableHasErrors = tables[activeIndex].hasErrors;
    // }
    // let tabs = validationLevels
    //   ? tables.map(table => {
    //       return (
    //         <TabPanel header={table.name} key={table.id} rightIcon={table.hasErrors ? config.icons['warning'] : null}>
    //           <div className={styles.tabsSchema}>
    //             <DataViewer
    //               buttonsList={buttonsList}
    //               levelErrorTypes={levelErrorTypes}
    //               hasWritePermissions={hasWritePermissions}
    //               isDataCollection={isDataCollection}
    //               isWebFormMMR={isWebFormMMR}
    //               key={table.id}
    //               isValidationSelected={isValidationSelected}
    //               onLoadTableData={onLoadTableData}
    //               tableHasErrors={tableHasErrors}
    //               tableId={table.id}
    //               tableName={table.name}
    //             />
    //           </div>
    //         </TabPanel>
    //       );
    //     })
    //   : null;
    // const filterActiveIndex = tableSchemaId => {
    //   //TODO: Refactorizar este apaño y CUIDADO con activeIndex (integer cuando es manual, idTable cuando es por validación).
    //   if (Number.isInteger(tableSchemaId)) {
    //     return tabs ? activeIndex : 0;
    //   } else {
    //     return tabs ? tabs.findIndex(t => t.key === tableSchemaId) : 0;
    //   }
    // };

    // return (
    //   <TabView
    //     activeIndex={activeIndex ? filterActiveIndex(activeIndex) : 0}
    //     onTabChange={onTabChange}
    //     renderActiveOnly={false}>
    //     {tabs}
    //   </TabView>
    // );
  }
};
export { TabsValidations };
