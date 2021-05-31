import { useContext } from 'react';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';

import { BigButton } from 'ui/views/_components/BigButton';

const BigButtonListReference = () => {
  const resources = useContext(ResourcesContext);

  const newSchemaModel = [
    {
      label: resources.messages['createNewEmptyDatasetSchema'],
      icon: 'add'
      //   command: () => onShowNewSchemaDialog()
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

  return [newSchemaBigButton, ...buildGroupByRepresentativeModels([1, 2])].map(button => (
    <BigButton key={button.caption} {...button} />
  ));
};

export { BigButtonListReference };
