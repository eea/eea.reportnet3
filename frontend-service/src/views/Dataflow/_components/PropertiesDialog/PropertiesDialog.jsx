import { useContext, useEffect, useRef, useState } from 'react';

import uniqueId from 'lodash/uniqueId';

import styles from './PropertiesDialog.module.scss';

import { Button } from 'views/_components/Button';
import { Dialog } from 'views/_components/Dialog';
import { TreeView } from 'views/_components/TreeView';
import { TreeViewExpandableItem } from 'views/_components/TreeView/_components/TreeViewExpandableItem';

import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';
import { UserContext } from 'views/_functions/Contexts/UserContext';

import { PropertiesUtils } from './_functions/Utils/PropertiesUtils';
import { RodUrl } from 'repositories/config/RodUrl';

export const PropertiesDialog = ({ dataflowState, manageDialogs }) => {
  const resourcesContext = useContext(ResourcesContext);
  const userContext = useContext(UserContext);

  const [dialogHeight, setDialogHeight] = useState(null);

  const propertiesRef = useRef(null);

  useEffect(() => {
    if (propertiesRef.current && dataflowState.isPropertiesDialogVisible) {
      setDialogHeight(propertiesRef.current.getBoundingClientRect().height);
    }
  }, [propertiesRef.current, dataflowState.isPropertiesDialogVisible]);

  const parsedDataflowData = {
    dataflowName: dataflowState.name,
    dataflowDescription: dataflowState.description,
    dataflowStatus: dataflowState.data.status
  };
  const parsedObligationsData = PropertiesUtils.parseObligationsData(dataflowState, userContext.userProps.dateFormat);

  const dialogFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink button-right-aligned"
      icon="cancel"
      label={resourcesContext.messages['close']}
      onClick={() => manageDialogs('isPropertiesDialogVisible', false)}
    />
  );

  return (
    dataflowState.isPropertiesDialogVisible && (
      <Dialog
        className={styles.propertiesDialog}
        footer={dialogFooter}
        header={resourcesContext.messages['properties']}
        onHide={() => manageDialogs('isPropertiesDialogVisible', false)}
        visible={dataflowState.isPropertiesDialogVisible}>
        <div className={styles.propertiesWrap} ref={propertiesRef} style={{ height: dialogHeight }}>
          <div style={{ marginTop: '1rem', marginBottom: '2rem' }}>
            <TreeViewExpandableItem items={[{ label: resourcesContext.messages['dataflowDetails'] }]}>
              <TreeView property={parsedDataflowData} propertyName={''} />
            </TreeViewExpandableItem>
          </div>
          {parsedObligationsData.map((data, i) => (
            <div key={uniqueId()} style={{ marginTop: '2rem', marginBottom: '1rem' }}>
              <TreeViewExpandableItem
                buttons={[
                  {
                    className: `p-button-secondary-transparent`,
                    icon: 'externalUrl',
                    tooltip: resourcesContext.messages['viewMore'],
                    onMouseDown: () =>
                      window.open(
                        data.label === 'obligation'
                          ? `${RodUrl.obligations}${dataflowState.obligations.obligationId}`
                          : `${RodUrl.instruments}${dataflowState.obligations.legalInstruments.id}`
                      )
                  }
                ]}
                items={[{ label: PropertiesUtils.camelCaseToNormal(data.label) }]}>
                <TreeView property={data.data} propertyName={''} />
              </TreeViewExpandableItem>
            </div>
          ))}
        </div>
      </Dialog>
    )
  );
};
