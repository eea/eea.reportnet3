import React, { useContext } from 'react';

import styles from './PropertiesDialog.module.scss';

import { Button } from 'ui/views/_components/Button';
import { Dialog } from 'ui/views/_components/Dialog';
import { TreeView } from 'ui/views/_components/TreeView';
import { TreeViewExpandableItem } from 'ui/views/_components/TreeView/_components/TreeViewExpandableItem';

import { ResourcesContext } from 'ui/views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'ui/views/_functions/Contexts/UserContext';

import { PropertiesUtils } from './_functions/Utils/PropertiesUtils';

export const PropertiesDialog = ({ dataflowState, manageDialogs }) => {
  const resources = useContext(ResourcesContext);
  const user = useContext(UserContext);

  const parsedDataflowData = { dataflowStatus: dataflowState.data.status };
  const parsedObligationsData = PropertiesUtils.parseObligationsData(dataflowState, user.userProps.dateFormat);

  const dialogFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon="cancel"
      label={resources.messages['close']}
      onClick={() => manageDialogs('isPropertiesDialogVisible', false)}
    />
  );

  return (
    <Dialog
      className={styles.propertiesDialog}
      footer={dialogFooter}
      header={resources.messages['properties']}
      onHide={() => manageDialogs('isPropertiesDialogVisible', false)}
      visible={dataflowState.isPropertiesDialogVisible}>
      <div className={styles.propertiesWrap}>
        {dataflowState.description}
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
                        ? `http://rod3.devel1dub.eionet.europa.eu/obligations/${dataflowState.obligations.obligationId}`
                        : `http://rod3.devel1dub.eionet.europa.eu/instruments/${dataflowState.obligations.legalInstruments.id}`
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
