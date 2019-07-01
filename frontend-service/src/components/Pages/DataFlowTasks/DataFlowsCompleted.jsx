import React, { useEffect, useContext, useState } from "react";
//import styles from "./DataFlowTasks.module.scss";
import DataFlowList from "./DataFlowList/DataFlowList";
import ResourcesContext from "../../Context/ResourcesContext";
import { ProgressSpinner } from "primereact/progressspinner";
//import HTTPRequesterAPI from '../../../services/HTTPRequester/HTTPRequester';

const DataFlowsCompleted = props => {
	const { listData } = props;
	const resources = useContext(ResourcesContext);
	const [completedDataFlows, setCompletedDataFlows] = useState([]);

	useEffect(() => {
		//GET JSON    --->   TODO implement this function with real API call

		setCompletedDataFlows([...listData]);
	}, [completedDataFlows, listData]);

	return (
		<DataFlowList
			listContent={completedDataFlows}
			listType="completed"
			listTitle={resources.messages["completedDataFlowTitle"]}
			listDescription={resources.messages["completedDataFlowText"]}
		/>
	);
};
export default DataFlowsCompleted;
