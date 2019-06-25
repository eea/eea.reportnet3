import React from 'react'
import DataFlowItem from './DataFlowItem/DataFlowItem'

const DataFlowList = (props) => {

    let arrayPending = props.pendingDataFlows;
    let arrayAccepted = props.acceptetDataFlows;

    if (arrayPending) {
        return (
            <DataFlowItem itemsArray = {arrayPending} isPending = {true} ></DataFlowItem>
        )
    } else if(arrayAccepted){
        return (
            <DataFlowItem  itemsArray = {arrayAccepted}  isPending = {false}></DataFlowItem>
        )
    }else{
        return (
            <div>
                No Data passed
            </div>
        )
    }
    
    
}

export default DataFlowList
