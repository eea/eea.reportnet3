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

    //Transpose value matrix and delete undefined elements
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
        intersect: false,
        callbacks: {
            label: (tooltipItems, data) => {
                    //var label = data.datasets[tooltipItems.datasetIndex].totalData || '';
                    console.log(tooltipItems.datasetIndex);
                    console.log(data);
                    console.log("Datasets")
                    console.log(data.datasets);
                    console.log(data.datasets[tooltipItems.datasetIndex].totalData); 
                    return `(${tooltipItems.yLabel} %)`}
        }
      },
      responsive: true,
      scales: {
          xAxes: [{
              stacked: true,
          }],
          yAxes: [{
              stacked: true,
              scaleLabel: {
								display: true
								//labelString: 'Value'
              },
              ticks: {
                // Include a % sign in the ticks
                callback: (value, index, values) => `${value} %`
            }
          }]
      }});
}, []);

    const getPercentage = (valArr) =>{
        //let valArr = [[105, 50, 80, 11],[15, 48, 58, 19],[10, 2, 15, 85]];     
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
