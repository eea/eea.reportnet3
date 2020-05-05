import React, { useContext, useEffect, useRef } from 'react';

import isNil from 'lodash/isNil';

import styles from './PropertiesDialog.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { TreeView } from 'ui/views/_components/TreeView';
import { TreeViewExpandableItem } from 'ui/views/_components/TreeView/_components/TreeViewExpandableItem';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { PropertiesUtils } from './_functions/Utils/PropertiesUtils';

export const PropertiesDialog = ({ dataflowDataState, onManageDialogs }) => {
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

  const deleteInputRef = useRef(null);

  useEffect(() => {
    if (dataflowDataState.isDeleteDialogVisible && !isNil(deleteInputRef.current)) {
      deleteInputRef.current.element.focus();
    }
  }, [dataflowDataState.isDeleteDialogVisible]);

  const parsedDataflowData = { dataflowStatus: dataflowDataState.data.status };
  const parsedObligationsData = PropertiesUtils.parseObligationsData(dataflowDataState, user.userProps.dateFormat);

  const dialogFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon="cancel"
      label={resources.messages['close']}
      onClick={() => onManageDialogs('isPropertiesDialogVisible', false)}
    />
  );

  return (
    <Dialog
      className={styles.propertiesDialog}
      footer={dialogFooter}
      header={resources.messages['properties']}
      onHide={() => onManageDialogs('isPropertiesDialogVisible', false)}
      visible={dataflowDataState.isPropertiesDialogVisible}>
      <div className={styles.propertiesWrap}>
        {dataflowDataState.description}
        {parsedObligationsData.map((data, i) => (
          <div key={i} style={{ marginTop: '1rem', marginBottom: '2rem' }}>
            <TreeViewExpandableItem
              items={[{ label: PropertiesUtils.camelCaseToNormal(data.label) }]}
              buttons={[
                {
                  className: `p-button-secondary-transparent`,
                  icon: 'externalLink',
                  tooltip: resources.messages['viewMore'],
                  onMouseDown: () =>
                    window.open(
                      data.label === 'obligation'
                        ? `http://rod3.devel1dub.eionet.europa.eu/obligations/${dataflowDataState.obligations.obligationId}`
                        : `http://rod3.devel1dub.eionet.europa.eu/instruments/${dataflowDataState.obligations.legalInstruments.id}`
                    )
                }
              ]}>
              <TreeView property={data.data} propertyName={''} />
            </TreeViewExpandableItem>
          </div>
        ))}

        <TreeViewExpandableItem items={[{ label: resources.messages['dataflowDetails'] }]}>
          <TreeView property={parsedDataflowData} propertyName={''} />
        </TreeViewExpandableItem>
      </div>
    </Dialog>
  );
};
