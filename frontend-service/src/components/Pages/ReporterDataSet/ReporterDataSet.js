import React, {useState} from 'react';
import Title from '../../Layout/Title/Title';
import ButtonsBar from '../../Layout/UI/ButtonsBar/ButtonsBar';
import TabsSchema from '../../Layout/UI/TabsSchema/TabsSchema';
import styles from './ReporterDataSet.module.css';
import {Dialog} from 'primereact/dialog';
import {FileUpload} from 'primereact/fileupload';

const ReporterDataSet = () => {
  const [visible, setVisibility] = useState(false);

  const showFileUploadDialog = () => {
      console.log('showFileUploadDialog onClick');
      setVisibility(true);
  }

  const onUploadFile = () => {
      console.log('onUploadFile');
      // setVisibility(true);
  }

  const onHide = () => {
      console.log('onClick');
      setVisibility(false);
  }

  const onDrop = (event) => {
    console.log("onDrop");
    console.log(event);
    event.preventDefault();
  };

  //TODO:Change + Error/warning treatment
  let validationError = true;

  //const buttonClasses = (validationError)?".pi .pi-exclamation-triangle":"";
  const iconClasses = (validationError)?"warning":"";

  const customButtons = [
    {
      label: "Import",
      icon: "0",
      group: "left",
      disabled: false,
      clickHandler: showFileUploadDialog
    },
    {
      label: "Export",
      icon: "1",
      group: "left",
      disabled: false,
      clickHandler: null
    },
    {
      label: "Delete",
      icon: "2",
      group: "left",
      disabled: false,
      clickHandler: null
    },
    {
      label: "Events",
      icon: "4",
      group: "right",
      disabled: false,
      clickHandler: null
    },
    {
      label: "Validations",
      icon: "3",
      group: "right",
      disabled: !validationError,
      clickHandler: null,
      ownButtonClasses:null,
      iconClasses:iconClasses
    },
    {
      label: "Dashboards",
      icon: "5",
      group: "right",
      disabled: false,
      clickHandler: null
    },
  ];

  return (
    <div >
        <Title title="Data set: Bathing Water" /> 
        <div className={styles.ButtonsBar}>      
          <ButtonsBar buttons={customButtons} />
        </div>
        <TabsSchema tables={[
          { name: "Table 1" },
          { name: "Table 2" },
          { name: "Table 3" },
          { name: "Table 4" }]} />
          <Dialog header="Upload your Dataset" visible={visible}
                  className={styles.Dialog} onHide={onHide} >
              <FileUpload mode="advanced" name="demo[]" url="./upload.php" onUpload={onUploadFile} 
                          multiple={false} allowTypes="/(\.|\/)(csv|doc)$/" chooseLabel="Select or drag here your dataset (.csv)"
                          fileLimit={1} className={styles.FileUpload}  /> 
          </Dialog>
      </div>
  );
}

export default ReporterDataSet;
