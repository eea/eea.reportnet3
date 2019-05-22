import React from 'react';
import Title from '../../Layout/Title/Title';
import ButtonsBar from '../../Layout/UI/ButtonsBar/ButtonsBar';
import TabsSchema from '../../Layout/UI/TabsSchema/TabsSchema';
import styles from './ReporterDataSet.module.css';

const ReporterDataSet = () => {
  
  const clickTest = () => {
    console.log("Click on button");
  }

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
      clickHandler: clickTest
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
        <Title title="Reporting Data Set: R3 Demo Dataflow" /> 
        <div className={styles.ButtonsBar}>      
          <ButtonsBar buttons={customButtons} />
        </div>
        <TabsSchema tables={[
          { name: "Table 1" }, 
          { name: "Table 2" },
          { name: "Table 3" },
          { name: "Table 4" }]} />
      </div>
  );
}

export default ReporterDataSet;
