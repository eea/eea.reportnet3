import React, { useEffect, useContext, useState } from "react";

import styles from "./DataFlowTasks.module.scss";

import DataFlaws from "../../../assets/jsons/DataFlaws.json";
import ResourcesContext from "../../Context/ResourcesContext";

import DataFlowList from "./DataFlowList/DataFlowList";

import { BreadCrumb } from "primereact/breadcrumb";
import MainLayout from "../../Layout/main-layout.component";
import DataFlowColumn from "../../Layout/UI/DataFlowColumn/DataFlowColumn";
import { TabMenu } from "primereact/tabmenu";

//import HTTPRequesterAPI from '../../../services/HTTPRequester/HTTPRequester';
//import ResourcesContext from '../../Context/ResourcesContext';

const DataFlowTasks = () => {
	const resources = useContext(ResourcesContext);
	const [pendingDataFlows, setPendingDataFlows] = useState([]);
	const [acceptetDataFlows, setAcceptedDataFlows] = useState([]);
	const [tabMenuItems, setTabMenuItems] = useState([
		{ label: "Pendding" },
		{ label: "completed" }
	]);
	const [tabMenuActiveItem, setTabMenuActiveItem] = useState(tabMenuItems[0]);
	const home = { icon: resources.icons["home"], url: "/" };

	/*   const resources = useContext(ResourcesContext);  
  // This is here just for example purpose 
  const home = {icon: resources.icons["home"], url: '#'}; */

	useEffect(() => {
		//GET JSON    --->   TODO implement this function with real API call
		const jsonMimic = DataFlaws;

		const arrayPending = jsonMimic.filter(
			jsonData => jsonData.dataFlowStatus === "pending"
		);
		const arrayAccepted = jsonMimic.filter(
			jsonData => jsonData.dataFlowStatus === "accepted"
		);

		setPendingDataFlows([...arrayPending]);
		setAcceptedDataFlows([...arrayAccepted]);
	}, []);

	return (
		<MainLayout>
			<BreadCrumb
				model={[{ label: "Reporting data flow", url: "" }]}
				home={home}
			/>
			<div className="rep-container">
				<div className="rep-row">
					<DataFlowColumn navTitle="data flows" search={false} />
					<div className="subscribe-df rep-col-xs-12 rep-col-md-9">
						<TabMenu
							model={tabMenuItems}
							activeItem={tabMenuActiveItem}
							onTabChange={e => setTabMenuActiveItem(e.value)}
						/>
						<DataFlowList
							listContent={pendingDataFlows}
							listType="pending"
							listTitle="Pending data flows"
							listDescription="You are required to accept and report data to these data flows"
						/>
						<DataFlowList
							listContent={acceptetDataFlows}
							listType="accepted"
							listTitle="My data flows"
							listDescription="Please proceed to report before deadline"
						/>
					</div>
				</div>
			</div>
		</MainLayout>
	);
};
export default DataFlowTasks;
