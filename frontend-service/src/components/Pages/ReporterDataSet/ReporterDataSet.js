import React, {useState, useEffect} from 'react';

import Title from '../../Layout/Title/Title';
import ButtonsBar from '../../Layout/UI/ButtonsBar/ButtonsBar';
import TabsSchema from '../../Layout/UI/TabsSchema/TabsSchema';
import {Dialog} from 'primereact/dialog';
import {Chart} from 'primereact/chart';

import styles from './ReporterDataSet.module.css';

const ReporterDataSet = () => {
  const [dashDialogVisible, setDashDialogVisible] = useState(false);
  const [customButtons, setCustomButtons] = useState([]);
  const [validationError, setValidationError] = useState(true);
  const [dashBoardData, setDashBoardData] = useState({});
  const [dashBoardOptions, setDashBoardOptions] = useState({});

  const onDashBoardClickHandler = () =>{
    setDashDialogVisible(true);
  } 
  const onHideDialogHandler = () =>{
    setDashDialogVisible(false);
  } 

  useEffect(()=>{
    console.log("ReporterDataSet useEffect");
    setCustomButtons([
      {
        label: "Import",
        icon: "0",
        group: "left",
        disabled: false,
        clickHandler: null
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
        iconClasses:(validationError)?"warning":""
      },
      {
        label: "Dashboards",
        icon: "5",
        group: "right",
        disabled: false,
        clickHandler: onDashBoardClickHandler
      },
    ]);
    //TODO:Change + Error/warning treatment

    setDashBoardData({
      labels: ['Table 1', 'Table 2', 'Table 3', 'Table 4'],
            datasets: [
                {
                    label: 'Info',
                    backgroundColor: '#004494',
                    data: [65, 50, 80, 11]
                },
                {
                    label: 'Warning',
                    backgroundColor: '#ffd617',
                    data: [15, 48, 5, 19]
                },
                {
                  label: 'Error',
                  backgroundColor: '#DA2131',
                  data: [10, 2, 15, 70]
              }
            ]});

  setDashBoardOptions({tooltips: {
    mode: 'index',
    intersect: false
},
responsive: true,
scales: {
    xAxes: [{
        stacked: true,
    }],
    yAxes: [{
        stacked: true
    }]
}});

    // {
    //   tooltips: {
    //       // text: '',
    //       // data: {"Test 1" : 40, "Test 2" : 40, "Test 3" : 20 }, 
    //       label: {"aaa": 20},
    //       mode: 'index',
    //       intersect: false
    //   },
    //   // style: 
    //   // width: 0.5,
    //   responsive: true,
    //   position: 'center',
    //   legend: {
    //       position: 'bottom'
    //       },
    //   scales: {
    //       xAxes: [{
    //           stacked: true,
    //       }],
    //       yAxes: [{
    //           type: 'linear',
    //           display: true,
    //           position: 'left',
    //           id: 'y-axis-1',
    //           stacked: true,
    //           ticks: {
    //               min: 0,
    //               max: 100
    //           }
    //       }]
    //   }
    // });

  },[]);

  
console.log('ReporterDataSet');

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
        <Dialog visible={dashDialogVisible} onHide={onHideDialogHandler} header="Error/Warning dashboard" maximizable dismissableMask={true} style={{width:'80%'}}>
          <Chart type="bar" data={dashBoardData} options={dashBoardOptions} />
        </Dialog>
      </div>
  );
}

export default ReporterDataSet;
