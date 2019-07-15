import React, { useState, useEffect, useContext } from 'react';
import {withRouter} from 'react-router-dom';
import {Chart} from 'primereact/chart';

import HTTPRequester from '../../services/HTTPRequester/HTTPRequester';
import styles from './DashBoard.module.css';
import config from '../../conf/web.config.json';
import ResourcesContext from '../../components/Context/ResourcesContext';

const DashBoard = (props) =>{
    const {match:{params: { dataSetId } }} = props;
    const [dashBoardData, setDashBoardData] = useState({});
    const [dashBoardOptions, setDashBoardOptions] = useState({});
    const [dashBoardTitle, setDashBoardTitle] = useState("");
    const resources = useContext(ResourcesContext); 

    useEffect(() => {   
    
        //TODO: Darle una vuelta a esto. ¿Props en el useEffect? Y el if es cutre a más no poder.
        if(props.refresh){
            //'/jsons/error-statistics.json'
            const dataPromise = HTTPRequester.get(
            {
                url: `${config.loadStatisticsAPI.url}${dataSetId}`,
                queryString: {}
            });

            //Parse JSON to array statistic values
            dataPromise
            .then(res =>{
                if(res.data.tables !== null){
                    const tabStatisticNames = [];
                    const tabStatisticValues = [];
                    setDashBoardTitle(res.data.nameDataSetSchema);
                    res.data.tables.forEach(t => {
                        tabStatisticNames.push(t.nameTableSchema);
                        tabStatisticValues.push([t.totalRecords-(t.totalRecordsWithErrors+t.totalRecordsWithWarnings),t.totalRecordsWithWarnings,t.totalRecordsWithErrors]);
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
                }
            })
            .catch(error=>{
                console.log(error);
                return error
            })
        }
    
}, [props.refresh]);

    const getPercentage = (valArr) =>{  
        let total = valArr.reduce((arr1, arr2) =>
            arr1.map((v, i) => v + arr2[i]));
        return valArr.map(val=>(val.map((v,i)=>((v/total[i])*100).toFixed(2))));
    }


    return(
        <React.Fragment>
        <h1>{dashBoardTitle}</h1>
        { (dashBoardData.datasets && dashBoardData.datasets.length > 0) ?
          <Chart type="bar" 
                 data={dashBoardData} 
                 options={dashBoardOptions} />
        : <div className={styles.NoErrorData}>{resources.messages["noErrorData"]}</div>
            }
        </React.Fragment>
    );
}

export default withRouter(React.memo(DashBoard));
