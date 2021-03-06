import { Fragment, useContext, useEffect, useState } from 'react';
import { withRouter } from 'react-router-dom';

import isEmpty from 'lodash/isEmpty';
import isNil from 'lodash/isNil';
import isNull from 'lodash/isNull';
import isUndefined from 'lodash/isUndefined';

import { config } from 'conf';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { FieldsDesigner } from './_components/FieldsDesigner';
import { getUrl } from 'core/infrastructure/CoreUtils';
import { routes } from 'ui/routes';
import { TabView } from 'ui/views/_components/TabView';
import { TabPanel } from 'ui/views/_components/TabView/_components/TabPanel';

import { DatasetService } from 'core/services/Dataset';

import { NotificationContext } from 'ui/views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { DatasetDesignerUtils } from 'ui/views/DatasetDesigner/_functions/Utils/DatasetDesignerUtils';
import { QuerystringUtils } from 'ui/views/_functions/Utils/QuerystringUtils';
import { TabsUtils } from 'ui/views/_functions/Utils/TabsUtils';
import { TextUtils } from 'ui/views/_functions/Utils/TextUtils';

export const TabsDesigner = withRouter(
  ({
    changeMode,
    datasetSchema,
    datasetSchemas,
    datasetStatistics,
    editable = false,
    getIsTableCreated,
    getUpdatedTabs,
    history,
    isDataflowOpen,
    isDesignDatasetEditorRead,
    isGroupedValidationDeleted,
    isGroupedValidationSelected,
    isReferenceDataset,
    isValidationSelected,
    manageDialogs,
    manageUniqueConstraint,
    match,
    onChangeIsValidationSelected,
    onChangeReference,
    onHideSelectGroupedValidation,
    onLoadTableData,
    onTabChange,
    onUpdateSchema,
    onUpdateTable,
    recordPositionId,
    selectedRecordErrorId,
    selectedRuleId,
    selectedRuleLevelError,
    selectedRuleMessage,
    selectedTableSchemaId,
    setActiveTableSchemaId,
    tableSchemaId,
    viewType
  }) => {
    const {
      params: { dataflowId, datasetId }
    } = match;
    const notificationContext = useContext(NotificationContext);

    const [errorMessage, setErrorMessage] = useState();
    const [errorMessageTitle, setErrorMessageTitle] = useState();
    const [initialTabIndexDrag, setInitialTabIndexDrag] = useState();
    const [isEditing, setIsEditing] = useState(false);
    const [isErrorDialogVisible, setIsErrorDialogVisible] = useState(false);
    const [scrollFn, setScrollFn] = useState();
    const [tabs, setTabs] = useState([]);

    const resources = useContext(ResourcesContext);

    useEffect(() => {
      if (!isNil(datasetSchema) && !isEmpty(datasetSchema)) {
        onLoadSchema();
      }
    }, [datasetStatistics]);

    useEffect(() => {
      if (!isUndefined(scrollFn) && !isNull(scrollFn) && !isEditing) {
        scrollFn();
      }
    }, [scrollFn, tabs, isEditing]);

    useEffect(() => {
      if (!isEmpty(tabs)) {
        getUpdatedTabs(tabs);
        onUpdateTable(tabs);
        onUpdateSchema(tabs);
      }
    }, [tabs]);

    useEffect(() => {
      if (isErrorDialogVisible) {
        renderErrors(errorMessageTitle, errorMessage);
      }
    }, [isErrorDialogVisible]);

    const onChangeFields = (fields, isLinkChange, tabSchemaId) => {
      const inmTabs = [...tabs];
      const tabIdx = TabsUtils.getIndexByTableProperty(tabSchemaId, inmTabs, 'tableSchemaId');
      if (!isNil(inmTabs[tabIdx].records)) {
        inmTabs[tabIdx].records[0].fields = fields;
        setTabs(inmTabs);
      } else {
        inmTabs[tabIdx].records = [];
        inmTabs[tabIdx].records[0] = {};
        inmTabs[tabIdx].records[0].fields = fields;
      }
      if (isLinkChange) {
        onChangeReference(inmTabs, datasetSchema.datasetSchemaId);
      }
    };

    const onChangeTableProperties = (
      tabSchemaId,
      tableSchemaDescription,
      readOnly,
      toPrefill,
      notEmpty,
      fixedNumber
    ) => {
      const inmTabs = [...tabs];
      const tabIdx = TabsUtils.getIndexByTableProperty(tabSchemaId, inmTabs, 'tableSchemaId');
      inmTabs[tabIdx].description = tableSchemaDescription;
      inmTabs[tabIdx].fixedNumber = fixedNumber;
      inmTabs[tabIdx].notEmpty = notEmpty;
      inmTabs[tabIdx].readOnly = readOnly;
      inmTabs[tabIdx].toPrefill = toPrefill;
      setTabs(inmTabs);
    };

    const onLoadSchema = async () => {
      try {
        setTabs(
          DatasetDesignerUtils.getTabs({
            datasetSchema,
            datasetStatistics,
            editable,
            isDataflowOpen,
            isDesignDatasetEditorRead
          })
        );
      } catch (error) {
        console.error(`Error while loading schema ${error}`);
        if (!isUndefined(error.response) && (error.response.status === 401 || error.response.status === 403)) {
          history.push(getUrl(routes.DATAFLOWS, true));
        }
      }
    };

    const onTabAdd = (newTabElement, onTabAddCallback) => {
      //Add a temporary Tab with an input text
      if (!checkEditingTabs()) {
        const inmTabs = [...tabs];
        inmTabs.push({ ...newTabElement, index: TabsUtils.getMaxIndex([...tabs]) + 1 });
        [inmTabs[inmTabs.length - 1], inmTabs[inmTabs.length - 2]] = [
          inmTabs[inmTabs.length - 2],
          inmTabs[inmTabs.length - 1]
        ];
        setScrollFn(() => onTabAddCallback);
        setTabs(inmTabs);
      }
    };

    const onTabAddCancel = () => {
      if (!isErrorDialogVisible) {
        const inmTabs = [...tabs];
        const newTab = tabs.filter(tab => tab.newTab);
        const filteredTabs = inmTabs.filter(inmTab => inmTab.index !== newTab[0].index);
        setTabs([...filteredTabs]);
      }
    };

    const onTabClicked = event => {
      if (event.header !== '') {
        setActiveTableSchemaId(event.tableSchemaId);
        onChangeIsValidationSelected({ isValidationSelected: false, isGroupedValidationSelected });
      }
    };

    const onTabEditingHeader = editing => setIsEditing(editing);

    const onTableAdd = (header, tabIndex, initialHeader) => {
      if (header !== initialHeader) {
        if (checkDuplicates(header, tabIndex)) {
          setErrorMessageTitle(resources.messages['duplicatedTabHeader']);
          setErrorMessage(resources.messages['duplicatedTabHeaderError']);
          setIsErrorDialogVisible(true);
          return { correct: false, tableName: header };
        } else if (checkInvalidCharacters(header)) {
          setErrorMessageTitle(resources.messages['invalidCharactersTabHeader']);
          setErrorMessage(resources.messages['invalidCharactersTabHeaderError']);
          setIsErrorDialogVisible(true);
          return { correct: false, tableName: header };
        } else {
          if (tabs[tabIndex].newTab) {
            addTable(header, tabIndex);
            changeMode('design');
          } else {
            updateTableName(tabs[tabIndex].tableSchemaId, header);
          }
        }
      }
    };

    const onTableDelete = deletedTabTableSchemaId => deleteTable(deletedTabTableSchemaId);

    const onTableDragAndDrop = (draggedTabHeader, droppedTabHeader) => reorderTable(draggedTabHeader, droppedTabHeader);

    const onTableDragAndDropStart = (draggedTabIdx, draggedTabId) => {
      if (!isUndefined(draggedTabId)) {
        setActiveTableSchemaId(draggedTabId);
      } else {
        setActiveTableSchemaId(tabs[0].tableSchemaId);
      }
      setInitialTabIndexDrag(draggedTabIdx);
    };

    const onTabNameError = (errorTitle, error) => {
      setErrorMessageTitle(errorTitle);
      setErrorMessage(error);
      setIsErrorDialogVisible(true);
    };

    const addTable = async (header, tabIndex) => {
      try {
        const { data, status } = await DatasetService.addTableDesign(datasetId, header);

        if (status >= 200 && status <= 299) {
          const inmTabs = [...tabs];
          inmTabs[tabIndex].tableSchemaId = data.idTableSchema;
          inmTabs[tabIndex].recordId = data.recordSchema.idRecordSchema;
          inmTabs[tabIndex].recordSchemaId = data.recordSchema.idRecordSchema;
          inmTabs[tabIndex].header = header;
          inmTabs[tabIndex].tableSchemaName = header;
          inmTabs[tabIndex].newTab = false;
          inmTabs[tabIndex].showContextMenu = false;
          setActiveTableSchemaId(data.idTableSchema);
          setTabs(inmTabs);
          getIsTableCreated(true);
        }
      } catch (error) {
        console.error('Error during field Add: ', error);
        if (error?.response.status === 400) {
          if (error.response?.data?.message?.includes('name invalid')) {
            notificationContext.add({
              type: 'DATASET_SCHEMA_TABLE_INVALID_NAME',
              content: { tableName: header }
            });
          }
        }
      }
    };

    const arrayShift = (arr, initialIdx, endIdx) => {
      const element = arr[initialIdx];
      if (Math.abs(endIdx - initialIdx) > 1) {
        arr.splice(initialIdx, 1);
        if (initialIdx < endIdx) {
          arr.splice(endIdx - 1, 0, element);
        } else {
          arr.splice(endIdx, 0, element);
        }
      } else {
        if (endIdx === 0) {
          arr.splice(initialIdx, 1);
          arr.splice(0, 0, element);
        } else {
          arr.splice(initialIdx, 1);
          if (initialIdx < endIdx) {
            arr.splice(endIdx - 1, 0, element);
          } else {
            arr.splice(endIdx, 0, element);
          }
        }
      }
      return arr;
    };

    const checkDuplicates = (header, tabIndex) => {
      const inmTabs = [...tabs];
      const repeatedElements = inmTabs.filter(tab => TextUtils.areEquals(header, tab.header));
      return repeatedElements.length > 0 && tabIndex !== repeatedElements[0].index;
    };

    const checkEditingTabs = () => {
      const inmTabs = [...tabs];
      const editingTabs = inmTabs.filter(tab => tab.newTab);
      return editingTabs.length > 0;
    };

    const checkInvalidCharacters = header => {
      const invalidCharsRegex = new RegExp(/[^a-zA-Z0-9_-\s]/);
      return invalidCharsRegex.test(header);
    };

    const deleteTable = async deletedTableSchemaId => {
      try {
        const response = await DatasetService.deleteTableDesign(datasetId, deletedTableSchemaId);
        if (response.status >= 200 && response.status <= 299) {
          const inmTabs = [...tabs];
          const deletedTabIndx = TabsUtils.getIndexByTableProperty(deletedTableSchemaId, inmTabs, 'tableSchemaId');
          inmTabs.splice(deletedTabIndx, 1);
          inmTabs.forEach(tab => {
            if (tab.addTab) {
              tab.index = -1;
              tab.tableSchemaId = '';
            }
          });
          if (tableSchemaId === deletedTableSchemaId) {
            setActiveTableSchemaId(inmTabs[0].tableSchemaId);
          }
          onChangeReference(inmTabs, datasetSchema.datasetSchemaId);
          setTabs(inmTabs);
        }
      } catch (error) {
        console.error('error', error);
      }
    };

    const errorDialogFooter = (
      <div className="ui-dialog-buttonpane p-clearfix">
        <Button icon="check" label={resources.messages['ok']} onClick={() => setIsErrorDialogVisible(false)} />
      </div>
    );

    const renderErrors = (errorTitle, error) => {
      return (
        isErrorDialogVisible && (
          <Dialog
            footer={errorDialogFooter}
            header={errorTitle}
            modal={true}
            onHide={() => setIsErrorDialogVisible(false)}
            visible={isErrorDialogVisible}>
            <div className="p-grid p-fluid">{error}</div>
          </Dialog>
        )
      );
    };

    const renderTabViews = () => {
      const idx = TabsUtils.getIndexByTableProperty(
        !isNil(tableSchemaId)
          ? tableSchemaId
          : QuerystringUtils.getUrlParamValue('tab') !== ''
          ? QuerystringUtils.getUrlParamValue('tab')
          : 0,
        tabs,
        'tableSchemaId'
      );
      return (
        <TabView
          activeIndex={idx !== -1 ? idx : 0}
          checkEditingTabs={checkEditingTabs}
          designMode={true}
          history={history}
          initialTabIndexDrag={initialTabIndexDrag}
          isDataflowOpen={isDataflowOpen}
          isDesignDatasetEditorRead={isDesignDatasetEditorRead}
          isErrorDialogVisible={isErrorDialogVisible}
          onTabAdd={onTabAdd}
          onTabAddCancel={onTabAddCancel}
          onTabBlur={onTableAdd}
          onTabChange={onTabChange}
          onTabClick={onTabClicked}
          onTabConfirmDelete={onTableDelete}
          onTabDragAndDrop={onTableDragAndDrop}
          onTabDragAndDropStart={onTableDragAndDropStart}
          onTabEditingHeader={onTabEditingHeader}
          onTabNameError={onTabNameError}
          tableSchemaId={tableSchemaId}
          tabs={tabs}
          totalTabs={tabs.length}
          viewType={viewType}>
          {tabs.length > 0
            ? tabs.map((tab, i) => {
                return (
                  <TabPanel
                    addTab={tab.addTab}
                    editable={tab.editable}
                    hasPKReferenced={tab.hasPKReferenced}
                    header={tab.header}
                    index={tab.index}
                    key={tab.index}
                    newTab={tab.newTab}
                    rightIcon={tab.hasErrors ? config.icons['warning'] : null}
                    tableSchemaId={tab.tableSchemaId}>
                    {(tabs.length > 0 && (isDataflowOpen || isDesignDatasetEditorRead)) || tabs.length > 1 ? (
                      <FieldsDesigner
                        autoFocus={false}
                        dataflowId={dataflowId}
                        datasetId={datasetId}
                        datasetSchemaId={datasetSchema.datasetSchemaId}
                        datasetSchemas={datasetSchemas}
                        isDataflowOpen={isDataflowOpen}
                        isDesignDatasetEditorRead={isDesignDatasetEditorRead}
                        isGroupedValidationDeleted={isGroupedValidationDeleted}
                        isGroupedValidationSelected={isGroupedValidationSelected}
                        isReferenceDataset={isReferenceDataset}
                        isValidationSelected={isValidationSelected}
                        key={tab.index}
                        manageDialogs={manageDialogs}
                        manageUniqueConstraint={manageUniqueConstraint}
                        onChangeFields={onChangeFields}
                        onChangeIsValidationSelected={onChangeIsValidationSelected}
                        onChangeReference={onChangeReference}
                        onChangeTableProperties={onChangeTableProperties}
                        onHideSelectGroupedValidation={onHideSelectGroupedValidation}
                        onLoadTableData={onLoadTableData}
                        recordPositionId={tab.tableSchemaId === tableSchemaId ? recordPositionId : -1}
                        selectedRecordErrorId={tab.tableSchemaId === tableSchemaId ? selectedRecordErrorId : -1}
                        selectedRuleId={selectedRuleId}
                        selectedRuleLevelError={selectedRuleLevelError}
                        selectedRuleMessage={selectedRuleMessage}
                        selectedTableSchemaId={selectedTableSchemaId}
                        table={tabs[i]}
                        viewType={viewType}
                      />
                    ) : (
                      <h3>{`${resources.messages['datasetDesignerAddTable']}`}</h3>
                    )}
                  </TabPanel>
                );
              })
            : null}
        </TabView>
      );
    };

    const reorderTable = async (draggedTabHeader, droppedTabHeader) => {
      try {
        const inmTabs = [...tabs];
        const draggedTabIdx = TabsUtils.getIndexByHeader(draggedTabHeader, inmTabs);
        const droppedTabIdx = TabsUtils.getIndexByHeader(droppedTabHeader, inmTabs);
        const tableOrdered = await DatasetService.orderTableDesign(
          datasetId,
          draggedTabIdx > droppedTabIdx ? droppedTabIdx : droppedTabIdx - 1,
          tabs[draggedTabIdx].tableSchemaId
        );
        if (tableOrdered.status >= 200 && tableOrdered.status <= 299) {
          const shiftedTabs = arrayShift(inmTabs, draggedTabIdx, droppedTabIdx);

          shiftedTabs.forEach((tab, i) => (tab.index = !tab.addTab ? i : -1));
          setActiveTableSchemaId(
            shiftedTabs[draggedTabIdx > droppedTabIdx ? droppedTabIdx : droppedTabIdx - 1].tableSchemaId
          );
          setTabs([...shiftedTabs]);
        }
      } catch (error) {
        console.error(`There has been an error while ordering tables ${error}`);
      }
    };

    const updateTableName = async (tableSchemaId, tableSchemaName) => {
      try {
        const { status } = await DatasetService.updateTableNameDesign(tableSchemaId, tableSchemaName, datasetId);
        if (status >= 200 && status <= 299) {
          const inmTabs = [...tabs];
          inmTabs[TabsUtils.getIndexByTableProperty(tableSchemaId, inmTabs, 'tableSchemaId')].header = tableSchemaName;
          inmTabs[
            TabsUtils.getIndexByTableProperty(tableSchemaId, inmTabs, 'tableSchemaId')
          ].tableSchemaName = tableSchemaName;
          setTabs(inmTabs);
        }
      } catch (error) {
        console.error('error', error);
        if (error?.response.status === 400) {
          if (error.response?.data?.message?.includes('name invalid')) {
            notificationContext.add({
              type: 'DATASET_SCHEMA_TABLE_INVALID_NAME',
              content: { tableName: tableSchemaName }
            });
          }
        }
      }
    };

    return (
      <Fragment>
        {renderTabViews()}
        {renderErrors(errorMessageTitle, errorMessage)}
      </Fragment>
    );
  }
);
