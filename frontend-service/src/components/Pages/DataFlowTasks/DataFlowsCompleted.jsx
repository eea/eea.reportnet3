import React, { useEffect, useContext, useState } from "react";
//import styles from "./DataFlowTasks.module.scss";
import DataFlowList from "./DataFlowList/DataFlowList";
import DataFlawsCompleted from "../../../assets/jsons/DataFlawsCompleted.json";
import MainLayout from "../../Layout/main-layout.component";
import DataFlowColumn from "../../Layout/UI/DataFlowColumn/DataFlowColumn";
import ResourcesContext from "../../Context/ResourcesContext";
import { ProgressSpinner } from "primereact/progressspinner";
//import HTTPRequesterAPI from '../../../services/HTTPRequester/HTTPRequester';

const DataFlowsCompleted = () => {
	const resources = useContext(ResourcesContext);
	const [completedDataFlows, setCompletedDataFlows] = useState([]);
	const [loading, setLoading] = useState(true);

	useEffect(() => {
		//GET JSON    --->   TODO implement this function with real API call
		const jsonMimic = DataFlawsCompleted;

		setCompletedDataFlows([...jsonMimic]);
		setLoading(false);
	}, [completedDataFlows]);

	if (loading) {
		return <ProgressSpinner />;
	}

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
