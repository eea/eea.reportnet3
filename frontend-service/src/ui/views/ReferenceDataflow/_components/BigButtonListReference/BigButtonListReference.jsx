import { Fragment, useContext, useReducer } from 'react';
import { withRouter } from 'react-router';

import isNil from 'lodash/isNil';

import { config } from 'conf';
import { routes } from 'ui/routes';

import styles from './BigButtonListReference.module.scss';

import { BigButton } from 'ui/views/_components/BigButton';
import { ConfirmDialog } from 'ui/views/_components/ConfirmDialog';
import { Dialog } from 'ui/views/_components/Dialog';
import { NewDatasetSchemaForm } from 'ui/views/_components/NewDatasetSchemaForm';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { referenceBigButtonsReducer } from './_functions/Reducers/referenceBigButtonsReducer';

import { getUrl } from 'core/infrastructure/CoreUtils';

const BigButtonListReference = withRouter(({ dataflowId, dataflowState, history, onSaveName, onUpdateData }) => {
  const isDesignStatus = dataflowState.status === config.dataflowStatus.DESIGN;

  const resources = useContext(ResourcesContext);

  const [referenceBigButtonsState, referenceBigButtonsDispatch] = useReducer(referenceBigButtonsReducer, {
    dialogVisibility: { isCreateReference: false, isNewDataset: false }
  });

  const { dialogVisibility } = referenceBigButtonsState;

  const handleDialogs = ({ dialog, isVisible }) => {
    referenceBigButtonsDispatch({ type: 'HANDLE_DIALOGS', payload: { dialog, isVisible } });
  };

  const onToggleNewDatasetDialog = isVisible => handleDialogs({ dialog: 'isNewDataset', isVisible });

  const onRedirect = ({ params, route }) => history.push(getUrl(route, params, true));

  const newSchemaModel = [
    {
      command: () => handleDialogs({ dialog: 'isNewDataset', isVisible: true }),
      icon: 'add',
      label: resources.messages['createNewEmptyDatasetSchema']
    },
    { disabled: true, icon: 'clone', label: resources.messages['cloneSchemasFromDataflow'] },
    { disabled: true, icon: 'import', label: resources.messages['importSchema'] }
  ];

  const createDataCollection = {
    buttonClass: 'newItem',
    buttonIcon: 'siteMap',
    buttonIconClass: 'siteMapDisabled',
    caption: 'Covert to reference',
    handleRedirect: () => handleDialogs({ dialog: 'isCreateReference', isVisible: true }),
    helpClassName: 'dataflow-create-datacollection-help-step',
    layout: 'defaultBigButton',
    tooltip: 'tooltip',
    visibility: isDesignStatus
  };

  const newSchemaBigButton = {
    buttonClass: 'newItem',
    buttonIcon: 'plus',
    buttonIconClass: 'newItemCross',
    caption: resources.messages['newSchema'],
    helpClassName: 'dataflow-new-schema-help-step',
    layout: 'menuBigButton',
    model: newSchemaModel,
    visibility: isDesignStatus
  };

  const datasetsButtons = (datasets = []) => {
    const allDatasets = datasets.map(dataset => {
      return { datasetId: dataset.datasetId, datasetName: dataset.name, name: dataset.datasetSchemaName };
    });

    return allDatasets.map(dataset => ({
      buttonClass: 'dataset',
      buttonIcon: 'dataset',
      caption: dataset.datasetName,
      helpClassName: 'dataflow-dataset-help-step',
      handleRedirect: () => onRedirect({ route: routes.DATASET, params: { dataflowId, datasetId: dataset.datasetId } }),
      infoStatus: dataset.isReleased,
      infoStatusIcon: true,
      layout: 'defaultBigButton',
      onWheel: onRedirect({ route: routes.DATASET, params: { dataflowId, datasetId: dataset.datasetId } }),
      visibility: !isDesignStatus
    }));
  };

  const designDatasetButtons = isNil(dataflowState.data.designDatasets)
    ? []
    : dataflowState.data.designDatasets.map(newDatasetSchema => ({
        buttonClass: 'schemaDataset',
        buttonIcon: 'pencilRuler',
        caption: newDatasetSchema.datasetSchemaName,
        dataflowStatus: dataflowState.status,
        datasetSchemaInfo: dataflowState.updatedDatasetSchema,
        enabled: true,
        handleRedirect: () =>
          onRedirect({ route: routes.DATASET_SCHEMA, params: { dataflowId, datasetId: newDatasetSchema.datasetId } }),
        helpClassName: 'dataflow-schema-help-step',
        index: newDatasetSchema.index,
        layout: 'defaultBigButton',
        onSaveName: onSaveName,
        placeholder: resources.messages['datasetSchemaNamePlaceholder'],
        visibility: isDesignStatus
      }));

  const bigButtonList = [...designDatasetButtons, newSchemaBigButton, createDataCollection].map(button => (
    <BigButton key={button.caption} {...button} />
  ));

  return (
    <Fragment>
      <div className={styles.buttonsWrapper}>
        <div className={`${styles.splitButtonWrapper} dataflow-big-buttons-help-step`}>
          <div className={styles.datasetItem}>{bigButtonList}</div>
        </div>
      </div>

      {dialogVisibility.isNewDataset && (
        <Dialog
          className={styles.dialog}
          header={resources.messages['newDatasetSchema']}
          onHide={() => handleDialogs({ dialog: 'isNewDataset', isVisible: false })}
          visible={dialogVisibility.isNewDataset}>
          <NewDatasetSchemaForm
            dataflowId={dataflowId}
            datasetSchemaInfo={dataflowState.updatedDatasetSchema}
            onCreate={() => handleDialogs({ dialog: 'isNewDataset', isVisible: false })}
            onUpdateData={onUpdateData}
            setNewDatasetDialog={onToggleNewDatasetDialog}
          />
        </Dialog>
      )}

      {dialogVisibility.isCreateReference && (
        <ConfirmDialog
          header={'holi'}
          labelCancel={resources.messages['no']}
          labelConfirm={resources.messages['yes']}
          // onConfirm={onConfirmDeleteTable}
          onHide={() => handleDialogs({ dialog: 'isCreateReference', isVisible: false })}
          visible={dialogVisibility.isCreateReference}
        />
      )}
    </Fragment>
  );
});

export { BigButtonListReference };
