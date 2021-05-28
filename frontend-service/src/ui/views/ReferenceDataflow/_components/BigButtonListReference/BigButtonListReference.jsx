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
        :  */ 'defaultBigButton',
    // model: /* buttonsVisibility.cloneSchemasFromDataflow && !isCloningDataflow && !isImportingDataflow ? */ newSchemaModel, //: [],
    visibility: /* buttonsVisibility.newSchema */ true
  };
  return <BigButton {...newSchemaBigButton} />;
};

export { BigButtonListReference };
