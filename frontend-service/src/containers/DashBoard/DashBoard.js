import React, { useState, useEffect } from 'react';
import {Chart} from 'primereact/chart';

import jsonErrorStatistics from '../../assets/jsons/error-statistics.json';

const DashBoard = () =>{
    const [dashBoardData, setDashBoardData] = useState({});
    const [dashBoardOptions, setDashBoardOptions] = useState({});
    const [dashBoardTitle, setDashBoardTitle] = useState("");

useEffect(() => {
   //TODO HTTPAPI

    //Parse JSON to array statistic values
    const tabStatisticNames = [];
    const tabStatisticValues = [];
    setDashBoardTitle(jsonErrorStatistics.nameDataSetSchema);
    jsonErrorStatistics.tables.forEach(t => {
        tabStatisticNames.push(t.nameTableSchema);
	    tabStatisticValues.push([t.totalRecords-t.totalErrors,t.totalRecordsWithWarnings,t.totalRecordsWithErrors]);
    });

    //Transpose value matrix and delete undefined elements to fit Chart data structure
    const transposedValues = Object.keys(tabStatisticValues).map(c =>
      tabStatisticValues.map(r => r[c])
    ).filter(t=>t[0]!==undefined);
    
    setDashBoardData({
      labels: tabStatisticNames,
      datasets: [
          {
              label: 'Correct',
              backgroundColor: '#004494',
              data: getPercentage(transposedValues)[0],
              totalData: tabStatisticValues
          },
          {
              label: 'Warning',
              backgroundColor: '#ffd617',
              data: getPercentage(transposedValues)[1],
              totalData: tabStatisticValues
          },
          {
            label: 'Error',
            backgroundColor: '#DA2131',
            data: getPercentage(transposedValues)[2],
            totalData: tabStatisticValues
        }
      ]});

    setDashBoardOptions({
      tooltips: {
        mode: 'index',        
        callbacks: {
            label: (tooltipItems, data) => `${data.datasets[tooltipItems.datasetIndex].totalData[tooltipItems["index"]][tooltipItems.datasetIndex]} (${tooltipItems.yLabel} %)`}
      },
      responsive: true,
      scales: {
          xAxes: [{
              stacked: true,
              scaleLabel: {
                  display:true,
                  labelString: 'Tables'
              }
          }],
          yAxes: [{
              stacked: true,
              scaleLabel: {
                    display: true,
                    labelString: 'Percentage'
              },
              ticks: {
                // Include a % sign in the ticks
                callback: (value, index, values) => `${value} %`
            }
          }]
      }});
}, []);

    const getPercentage = (valArr) =>{  
        let total = valArr.reduce((arr1, arr2) =>
            arr1.map((v, i) => v + arr2[i]));
        return valArr.map(val=>(val.map((v,i)=>((v/total[i])*100).toFixed(2))));
    }


    return(
        <div>
        <h1>{dashBoardTitle}</h1>
          <Chart type="bar" 
                 data={dashBoardData} 
                 options={dashBoardOptions} />
                </div>
    );
}

export default React.memo(DashBoard);
