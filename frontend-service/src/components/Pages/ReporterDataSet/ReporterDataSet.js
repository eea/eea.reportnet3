import React from 'react';
import Title from '../../Layout/Title/Title';
import ButtonsBar from '../../Layout/UI/ButtonsBar/ButtonsBar';
import TabsSchema from '../../Layout/UI/TabsSchema/TabsSchema';
import styles from './ReporterDataSet.module.css';

const ReporterDataSet = () => {
  
  const clickTest = () => {
    console.log("Click on button");
  }

  const customButtons = [
    {
      label: "Import",
      icon: "0",
      group: "left",
      clickHandler: clickTest
    },
    {
      label: "Export",
      icon: "1",
      group: "left",
      clickHandler: null
    },
    {
      label: "Delete",
      icon: "2",
      group: "left",
      clickHandler: null
    },
    {
      label: "Events",
      icon: "4",
      group: "right",
      clickHandler: null
    },
    {
      label: "Validations",
      icon: "3",
      group: "right",
      clickHandler: null
    },
    {
      label: "Dashboards",
      icon: "5",
      group: "right",
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
