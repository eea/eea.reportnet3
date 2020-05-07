import React, { Fragment, useContext, useEffect, useRef, useState } from 'react';

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

  const [dialogHeight, setDialogHeight] = useState(null);

  const propertiesRef = useRef(null);

  useEffect(() => {
    if (propertiesRef.current && dataflowDataState.isPropertiesDialogVisible) {
      setDialogHeight(propertiesRef.current.getBoundingClientRect().height);
    }
  }, [propertiesRef.current, dataflowDataState.isPropertiesDialogVisible]);

  const parsedDataflowData = {
    dataflowName: dataflowDataState.name,
    dataflowDescription: dataflowDataState.description,
    dataflowStatus: dataflowDataState.data.status
  };
  const parsedObligationsData = PropertiesUtils.parseObligationsData(dataflowDataState, user.userProps.dateFormat);

  const dialogFooter = (
    <Button
      className="p-button-secondary p-button-animated-blink"
      icon="cancel"
      label={resources.messages['close']}
      onClick={() => onManageDialogs('isPropertiesDialogVisible', false)}
    />
  );

  return dataflowDataState.isPropertiesDialogVisible ? (
    <Dialog
      className={styles.propertiesDialog}
      footer={dialogFooter}
      header={resources.messages['properties']}
      onHide={() => onManageDialogs('isPropertiesDialogVisible', false)}
      visible={dataflowDataState.isPropertiesDialogVisible}>
      <div className={styles.propertiesWrap} ref={propertiesRef} style={{ height: dialogHeight }}>
        <div style={{ marginTop: '1rem', marginBottom: '2rem' }}>
          <TreeViewExpandableItem items={[{ label: resources.messages['dataflowDetails'] }]}>
            <TreeView property={parsedDataflowData} propertyName={''} />
          </TreeViewExpandableItem>
        </div>
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
      </div>
    </Dialog>
  ) : (
    <Fragment />
  );
};
