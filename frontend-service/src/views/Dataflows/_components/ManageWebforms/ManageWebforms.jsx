import { Fragment, useState, useEffect, useContext } from 'react';

import styles from './ManageWebforms.module.scss';

import { Dialog } from 'views/_components/Dialog';
import { Button } from 'views/_components/Button';

import { WebformService } from 'services/WebformService';

import { NotificationContext } from 'views/_functions/Contexts/NotificationContext';
import { ResourcesContext } from 'views/_functions/Contexts/ResourcesContext';

export const ManageWebforms = ({ onCloseDialog, isDialogVisible }) => {
  const resourcesContext = useContext(ResourcesContext);
  const notificationContext = useContext(NotificationContext);

  const [webforms, setWebforms] = useState([]);

  useEffect(() => {
    getWebformList();
  }, []);

  console.log(`webforms`, webforms);

  // Tabla de Webforms
  // => objeto de las columnas
  //                - id
  //                webformName
  //                actions
  //                        delete => botón de borrar y que devuelva un error si el webform está en uso en algún dataset
  //                          download => JSON
  //                          edit

  // Endpoints
  // create: un Post con el endpoint /webform/webformConfig. Espera un objeto WebformConfigVO que tiene "name" y "content", siendo content el json en string
  // update: un Put con el endpoint /webform/webformConfig. Espera un objeto WebformConfigVO que tiene name, content e idReferenced (el id del webform)
  // delete: un Delete con el endpoint /webform/webformConfig/{id} // Check that endpoint is correct

  const footer = (
    <Fragment>
      <Button
        className="p-button-primary"
        // disabled={isLoading}
        // icon={isLoading ? 'spinnerAnimate' : 'key'}
        icon={'plus'}
        label={resourcesContext.messages['add']}
        // onClick={}
      />
      <Button
        className="p-button-secondary p-button-right-aligned"
        icon="cancel"
        label={resourcesContext.messages['close']}
        onClick={onCloseDialog}
      />
    </Fragment>
  );

  const getWebformList = async () => {
    // setWebformOptionsLoadingStatus('pending');

    try {
      const data = await WebformService.getAll();
      setWebforms(data);
      //   setWebformOptionsLoadingStatus('success');
    } catch (error) {
      console.error('ManageWebforms - getWebformList.', error);
      //   setWebformOptionsLoadingStatus('failed');
      notificationContext.add({ type: 'LOADING_WEBFORM_OPTIONS_ERROR' }, true);
    }
  };
  return (
    <Dialog
      blockScroll={false}
      className="responsiveDialog"
      footer={footer}
      //   header={resourcesContext.messages['apiKeyDialogHead']}
      header="Manage Webforms "
      modal
      onHide={onCloseDialog}
      visible={isDialogVisible}>
      <div>content</div>
    </Dialog>
  );
};
