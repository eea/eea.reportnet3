import React, { useContext, useState, useEffect } from 'react';
import { withRouter } from 'react-router-dom';
import { isUndefined, isNull } from 'lodash';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
// import { FieldsDesigner } from './_components/TabView/_components/FieldsDesigner';
import { getUrl } from 'core/infrastructure/api/getUrl';
import { ResourcesContext } from 'ui/views/_components/_context/ResourcesContext';
import { routes } from 'ui/routes';
import { TabView } from './_components/TabView';
import { TabPanel } from './_components/TabView/_components/TabPanel';

import { DatasetService } from 'core/services/DataSet';

export const TabsDesigner = withRouter(({ match, history }) => {
  const {
    params: { dataflowId, datasetId }
  } = match;

  const [datasetSchema, setDatasetSchema] = useState();
  const [errorMessage, setErrorMessage] = useState();
  const [errorMessageTitle, setErrorMessageTitle] = useState();
  const [isEditing, setIsEditing] = useState(false);
  const [isErrorDialogVisible, setIsErrorDialogVisible] = useState(false);
  const [scrollFn, setScrollFn] = useState();
  const [tabs, setTabs] = useState([]);

  const resources = useContext(ResourcesContext);

  useEffect(() => {
    if (!isUndefined(scrollFn) && !isNull(scrollFn) && !isEditing) {
      scrollFn();
    }
  }, [scrollFn, tabs, isEditing]);

  useEffect(() => {
    onLoadSchema(dataflowId);
  }, []);

  useEffect(() => {
    if (!isUndefined(datasetSchema)) {
      setTabs(datasetSchema.tables);
    }
  }, [datasetSchema]);

  useEffect(() => {
    if (isErrorDialogVisible) {
      renderErrors(errorMessageTitle, errorMessage);
    }
  }, [isErrorDialogVisible]);

  const onTabEditingHeader = editing => {
    setIsEditing(editing);
  };

  const onLoadSchema = async dataflowId => {
    try {
      const datasetSchemaDTO = await DatasetService.schemaById(dataflowId);
      const inmDatasetSchema = { ...datasetSchemaDTO };
      inmDatasetSchema.tables.forEach((table, idx) => {
        table.editable = true;
        table.addTab = false;
        table.newTab = false;
        table.index = idx;
        table.header = table.tableSchemaName;
      });
      //Add tab Button/Tab
      inmDatasetSchema.tables.push({ header: '+', editable: false, addTab: true, newTab: false, index: -1 });
      setDatasetSchema(inmDatasetSchema);
    } catch (error) {
      if (error.response.status === 401 || error.response.status === 403) {
        history.push(getUrl(routes.DATAFLOWS, true));
      }
    } finally {
    }
  };

  const onTabAdd = (newTabElement, onTabAddCallback) => {
    //Add a temporary Tab with an input text
    const inmTabs = [...tabs];
    inmTabs.push({ ...newTabElement, index: getMaxIndex([...tabs]) + 1 });
    //Reorder tabs (actually by index, in the future by drag&drop order)
    let reorderedTabs = inmTabs.sort((a, b) => {
      return a.index < b.index ? -1 : a.index > b.index ? 1 : 0;
    });
    reorderedTabs.push(reorderedTabs.shift());

    setScrollFn(() => onTabAddCallback);
    setTabs(inmTabs);
  };

  const onTableAdd = (header, tabIndex, initialHeader) => {
    if (header !== initialHeader) {
      if (checkDuplicates(header, tabIndex)) {
        setErrorMessageTitle(resources.messages['duplicateTabHeader']);
        setErrorMessage(resources.messages['duplicateTabHeaderError']);
        setIsErrorDialogVisible(true);
        return { correct: false, tableName: header };
      } else {
        if (tabs[tabIndex].newTab) {
          addTable(header, tabIndex);
        } else {
          updateTableName(tabs[tabIndex].tableSchemaId, header);
        }
      }
    }
  };

  const onTabAddCancel = () => {
    if (!isErrorDialogVisible) {
      const inmTabs = [...tabs];
      const newTab = tabs.filter(tab => tab.newTab === true);
      const filteredTabs = inmTabs.filter(inmTab => inmTab.index !== newTab[0].index);
      setTabs([...filteredTabs]);
    }
  };

  const onTableDelete = deletedTabIndx => {
    deleteTable(deletedTabIndx);
  };

  const onTableDragAndDrop = (draggedTabHeader, droppedTabHeader) => {
    const inmTabs = [...tabs];
    const draggedTabIdx = getIndexByHeader(draggedTabHeader, inmTabs);
    const droppedTabIdx = getIndexByHeader(droppedTabHeader, inmTabs);

    const tabElement = inmTabs.filter(tab => tab.index === draggedTabIdx);
    const tabAuxElement = inmTabs.filter(tab => tab.index === droppedTabIdx);
    inmTabs[droppedTabIdx] = tabElement[0];
    inmTabs[draggedTabIdx] = tabAuxElement[0];
    setTabs([...inmTabs]);
  };

  const onTabNameError = (errorTitle, error) => {
    setErrorMessageTitle(errorTitle);
    setErrorMessage(error);
    setIsErrorDialogVisible(true);
  };

  const addTable = async (header, tabIndex) => {
    const tabledAdded = await DatasetService.addTableDesign(datasetSchema.datasetSchemaId, datasetId, header);
    if (tabledAdded) {
      onLoadSchema(dataflowId);
    } else {
      console.error('');
    }
  };

  const checkDuplicates = (header, tabIndex) => {
    const inmTabs = [...tabs];
    const repeteadElements = inmTabs.filter(tab => header.toLowerCase() === tab.header.toLowerCase());
    return repeteadElements.length > 0 && tabIndex !== repeteadElements[0].index;
  };

  const checkEditingTabs = () => {
    const inmTabs = [...tabs];
    const editingTabs = inmTabs.filter(tab => tab.newTab === true);
    return editingTabs.length > 0;
  };

  const deleteTable = async deletedTabIndx => {
    const tableDeleted = await DatasetService.deleteTableDesign(datasetId, tabs[deletedTabIndx].tableSchemaId);
    if (tableDeleted) {
      onLoadSchema(dataflowId);
    } else {
      console.error('');
    }
  };

  const getIndexByHeader = (header, tabsArray) => {
    return tabsArray
      .map(tab => {
        return tab.header;
      })
      .indexOf(header);
  };

  const getMaxIndex = tabsArray => {
    return Math.max(...tabsArray.map(tab => tab.index));
  };

  const renderErrors = (errorTitle, error) => {
    return (
      <Dialog
        footer={errorDialogFooter}
        header={errorTitle}
        modal={true}
        onHide={() => setIsErrorDialogVisible(false)}
        visible={isErrorDialogVisible}>
        <div className="p-grid p-fluid">{error}</div>
      </Dialog>
    );
  };

  const updateTableName = async (tableSchemaId, tableSchemaName) => {
    const tableUpdated = await DatasetService.updateTableNameDesign(
      datasetSchema.datasetSchemaId,
      tableSchemaId,
      tableSchemaName,
      datasetId
    );
    if (tableUpdated) {
      onLoadSchema(dataflowId);
    }
  };

  const errorDialogFooter = (
    <div className="ui-dialog-buttonpane p-clearfix">
      <Button
        label={resources.messages['ok']}
        icon="check"
        onClick={() => {
          setIsErrorDialogVisible(false);
        }}
      />
    </div>
  );

  return (
    <React.Fragment>
      <TabView
        checkEditingTabs={checkEditingTabs}
        onTabAdd={onTabAdd}
        onTabBlur={onTableAdd}
        onTabAddCancel={onTabAddCancel}
        onTabConfirmDelete={onTableDelete}
        onTabDragAndDrop={onTableDragAndDrop}
        onTabEditingHeader={onTabEditingHeader}
        onTabNameError={onTabNameError}>
        {tabs.length > 0
          ? tabs.map(tab => {
              return (
                <TabPanel
                  addTab={tab.addTab}
                  editable={tab.editable}
                  header={tab.header}
                  index={tab.index}
                  key={tab.index}
                  newTab={tab.newTab}>
                  {tabs.length > 1 ? (
                    // <FieldsDesigner
                    //   autoFocus={false}
                    //   datasetId={datasetId}
                    //   datasetSchemaId={datasetSchema.datasetSchemaId}
                    //   key={tab.index}
                    //   table={tabs[tab.index]}
                    // />
                    <div>
                      <h3>{resources.messages['datasetDesignerNoFields']}</h3>
                    </div>
                  ) : null}
                </TabPanel>
              );
            })
          : null}
      </TabView>
      {renderErrors(errorMessageTitle, errorMessage)}
    </React.Fragment>
  );
});
