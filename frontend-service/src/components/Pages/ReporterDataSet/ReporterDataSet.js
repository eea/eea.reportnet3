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
      console.log('onCick');
      setVisibility(false);
  }

  const onDragOver = (event) => {    
    //event.preventDefault();
    var filesBefore = document.getElementsByClassName("p-fileupload-row");
    var files = event.files;

    console.log("Dropped file. Length: " + files.length);
    console.log(files);
    console.log(filesBefore);
    console.log(event);

    if (filesBefore.length >= 1)
      {
        console.log("files.length > 0 (Length = " + files.length + ")");
        console.log(files);

        var fistFileNameValue = filesBefore[0].childNodes[0].textContent;
        console.log("First: " + fistFileNameValue);

        var lastFileNameValue = event.files[0].name;
        console.log("Last: " + lastFileNameValue);

        if (fistFileNameValue === lastFileNameValue) {
          console.log("Are equals, do not delete");
          // filesBefore[0].remove();
        }
        else {
          console.log(filesBefore.length);
          console.log(filesBefore);
          // console.log(filesBefore);

          console.log("PRE - Delete");
          files[filesBefore.length -1].remove();
          // filesBefore[filesBefore.length -1].remove();
          console.log("POST - Delete");
        }
      }
  }

  // const onDrop = (event) => {
  //   const { completedTasks, draggedTask, todos } = this.state;
  //   this.setState({
  //     completedTasks: [...completedTasks, draggedTask],
  //     todos: todos.filter(task => task.taskID !== draggedTask.taskID),
  //     draggedTask: {},
  //   });
  // }


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
    <div>
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
                          fileLimit="0" className={styles.FileUpload} onSelect={onDragOver} />
          </Dialog>
      </div>
  );
}

export default ReporterDataSet;
