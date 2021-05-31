import { Fragment, useContext, useState } from 'react';

import styles from './BigButtonListReference.module.scss';

import { isNil } from 'lodash';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { BigButton } from 'ui/views/_components/BigButton';
import { Dialog } from 'ui/views/_components/Dialog';
import { NewDatasetSchemaForm } from 'ui/views/_components/NewDatasetSchemaForm';

const BigButtonListReference = ({ dataflowState, onShowNewSchemaDialog }) => {
  const [showNewDatasetDialog, setShowNewDatasetDialog] = useState(false);

  const resources = useContext(ResourcesContext);

  const newSchemaModel = [
    {
      label: resources.messages['createNewEmptyDatasetSchema'],
      icon: 'add',
      command: () => onShowNewSchemaDialog()
    },
    {
      disabled: true,
      label: resources.messages['cloneSchemasFromDataflow'],
      icon: 'clone'
      //   command: () => onCloneDataflow()
    },
    {
      label: resources.messages['importSchema'],
      icon: 'import'
      //   command: () => onImportSchema()
    }
  ];

  const newSchemaBigButton = {
    buttonClass: 'newItem',
    buttonIcon: /* isCloningDataflow || isImportingDataflow ? 'spinner' : */ 'plus',
    buttonIconClass: /* isCloningDataflow || isImportingDataflow ? 'spinner' :  */ 'newItemCross',
    caption: resources.messages['newSchema'],
    //   handleRedirect: !isCloningDataflow && !isImportingDataflow ? () => onShowNewSchemaDialog() : () => {},
    helpClassName: 'dataflow-new-schema-help-step',
    //   'defaultBigButton',
    layout:
      /*  buttonsVisibility.cloneSchemasFromDataflow && !isCloningDataflow && !isImportingDataflow
          ? 'menuBigButton'
        :  */ 'menuBigButton',
    model: /* buttonsVisibility.cloneSchemasFromDataflow && !isCloningDataflow && !isImportingDataflow ? */ newSchemaModel, //: [],
    visibility: /* buttonsVisibility.newSchema */ true
  };

  const buildGroupByRepresentativeModels = datasets => {
    const allDatasets = datasets.map((dataset, i) => {
      //i remove
      return {
        datasetId: i, // dataset.datasetId,
        datasetName: 'Name DS', //,dataset.name,
        dataProviderId: dataset, // dataset.dataProviderId,
        isReleased: true, //dataset.isReleased,
        name: 'Schema name' //dataset.datasetSchemaName
      };
    });

    return allDatasets.map(dataset => {
      return {
        buttonClass: 'dataset',
        buttonIcon: 'dataset',
        caption: dataset.datasetName,
        helpClassName: 'dataflow-dataset-help-step',
        // handleRedirect: () => {
        //   handleRedirect(getUrl(routes.DATASET, { dataflowId, datasetId: dataset.datasetId }, true));
        // },
        infoStatus: dataset.isReleased,
        infoStatusIcon: true,
        layout: 'defaultBigButton',
        model: [
          {
            label: resources.messages['historicReleases']
            // command: () => {
            //   onShowHistoricReleases('reportingDataset');
            //   getDataHistoricReleases(dataset.datasetId, dataset.datasetName);
            // }
          }
        ],
        // onWheel: getUrl(routes.DATASET, { dataflowId, datasetId: dataset.datasetId }, true),
        visibility: true
      };
    });
  };

  const designDatasetButtons = isNil(dataflowState.data.designDatasets)
    ? []
    : dataflowState.data.designDatasets.map(newDatasetSchema => ({
        buttonClass: 'schemaDataset',
        buttonIcon: 'pencilRuler',
        // canEditName: buttonsVisibility.designDatasetsActions,
        caption: newDatasetSchema.datasetSchemaName,
        dataflowStatus: dataflowState.status,
        datasetSchemaInfo: dataflowState.updatedDatasetSchema,
        enabled: true, //buttonsVisibility.designDatasetsActions || buttonsVisibility.designDatasetsOpen,
        handleRedirect:
          // buttonsVisibility.designDatasetsActions || buttonsVisibility.designDatasetsOpen
          //   ? () => {
          //       handleRedirect(
          //         getUrl(routes.DATASET_SCHEMA, { dataflowId, datasetId: newDatasetSchema.datasetId }, true)
          //       );
          //     }:
          () => {},

        helpClassName: 'dataflow-schema-help-step',
        index: newDatasetSchema.index,
        layout: 'defaultBigButton',
        // model:
        //   buttonsVisibility.designDatasetsActions || buttonsVisibility.designDatasetEditorReadActions
        //     ? [
        //         {
        //           label: resources.messages['openDataset'],
        //           icon: 'openFolder',
        //           command: () => {
        //             handleRedirect(
        //               getUrl(routes.DATASET_SCHEMA, { dataflowId, datasetId: newDatasetSchema.datasetId }, true)
        //             );
        //           }
        //         },
        //         {
        //           label: resources.messages['rename'],
        //           icon: 'pencil',
        //           disabled:
        //             dataflowState.status !== config.dataflowStatus.DESIGN || !buttonsVisibility.designDatasetsActions
        //         },
        //         {
        //           label: resources.messages['delete'],
        //           icon: 'trash',
        //           command: () => getDeleteSchemaIndex(newDatasetSchema.index),
        //           disabled:
        //             dataflowState.status !== config.dataflowStatus.DESIGN || !buttonsVisibility.designDatasetsActions
        //         }
        //       ]
        //     : [],
        // onSaveName: onSaveName,
        // onWheel: getUrl(routes.DATASET_SCHEMA, { dataflowId, datasetId: newDatasetSchema.datasetId }, true),
        placeholder: resources.messages['datasetSchemaNamePlaceholder'],
        // setErrorDialogData: setErrorDialogData,
        // tooltip:
        //   !buttonsVisibility.designDatasetsActions && !buttonsVisibility.designDatasetsOpen
        //     ? resources.messages['accessDenied']
        //     : '',
        visibility: true // buttonsVisibility.designDatasets || buttonsVisibility.designDatasetsOpen
      }));

  const bigButtonList = [
    ...designDatasetButtons,
    newSchemaBigButton
    /* ...buildGroupByRepresentativeModels([1, 2]) */
  ].map(button => <BigButton key={button.caption} {...button} />);

  return (
    <Fragment>
      <div className={styles.buttonsWrapper}>
        <div className={`${styles.splitButtonWrapper} dataflow-big-buttons-help-step`}>
          <div className={styles.datasetItem}>{bigButtonList}</div>
        </div>
      </div>

      {showNewDatasetDialog && (
        <Dialog
          className={styles.dialog}
          header={resources.messages['newDatasetSchema']}
          onHide={() => setShowNewDatasetDialog(false)}
          visible={showNewDatasetDialog}>
          <NewDatasetSchemaForm
            // dataflowId={dataflowId}
            datasetSchemaInfo={dataflowState.updatedDatasetSchema}
            // onCreate={onCreateDatasetSchema}
            // onUpdateData={onUpdateData}
            // setNewDatasetDialog={setNewDatasetDialog}
          />
        </Dialog>
      )}
    </Fragment>
  );
};

export { BigButtonListReference };
