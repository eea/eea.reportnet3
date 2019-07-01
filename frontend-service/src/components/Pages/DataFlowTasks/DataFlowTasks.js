import React, { useEffect, useContext, useState } from "react";
import styles from "./DataFlowTasks.module.scss";

import ResourcesContext from "../../Context/ResourcesContext";

import { BreadCrumb } from "primereact/breadcrumb";
import MainLayout from "../../Layout/main-layout.component";
import DataFlowColumn from "../../Layout/UI/DataFlowColumn/DataFlowColumn";
import { TabMenu } from "primereact/tabmenu";
import DataFlowsPendingAccepted from "./DataFlowsPendingAccepted";
import DataFlowsCompleted from "./DataFlowsCompleted";
import DataFlowList from "./DataFlowList/DataFlowList";
import DataFlawsCompleted from "../../../assets/jsons/DataFlawsCompleted.json";
import DataFlaws from "../../../assets/jsons/DataFlaws.json";

//import HTTPRequesterAPI from '../../../services/HTTPRequester/HTTPRequester';
//import ResourcesContext from '../../Context/ResourcesContext';

const DataFlowTasks = () => {
	const resources = useContext(ResourcesContext);

	const [tabMenuItems, setTabMenuItems] = useState([
		{ label: "pending" },
		{ label: "completed" }
	]);
	const [tabMenuActiveItem, setTabMenuActiveItem] = useState(tabMenuItems[0]);
	const [activeTab, setActiveTab] = useState("pending");
	const [tabData, setTabData] = useState([]);
	const home = { icon: resources.icons["home"], url: "/" };

	/*   const resources = useContext(ResourcesContext);  
  // This is here just for example purpose 
	const home = {icon: resources.icons["home"], url: '#'}; */

	useEffect(() => {
		const result = DataFlaws;
		const listData = [];
		if (tabMenuActiveItem.label === "pending") {
			listData.push({
				listContent: result.filter(data => data.dataFlowStatus === "pending"),
				listType: "pending",
				listTitle: resources.messages["pendingDataFlowTitle"],
				listDescription: resources.messages["pendingDataFlowText"]
			});
			listData.push({
				listContent: result.filter(data => data.dataFlowStatus === "accepted"),
				listType: "accepted",
				listTitle: resources.messages["acceptedDataFlowTitle"],
				listDescription: resources.messages["acceptedDataFlowText"]
			});
		} else {
			listData.push({
				listContent: result.filter(data => data.dataFlowStatus === "completed"),
				listType: "completed",
				listTitle: resources.messages["completedDataFlowTitle"],
				listDescription: resources.messages["completedDataFlowText"]
			});
		}
		setTabData(listData);
	}, [resources.messages, tabMenuActiveItem]);

	return (
		<MainLayout>
			<BreadCrumb
				model={[{ label: "Reporting data flow", url: "" }]}
				home={home}
			/>
			<div className="rep-container">
				<div className="rep-row">
					<DataFlowColumn
						navTitle={resources.messages["dataFlow"]}
						search={false}
					/>
					<div className={`${styles.container} rep-col-xs-12 rep-col-md-9`}>
						<TabMenu
							model={tabMenuItems}
							activeItem={tabMenuActiveItem}
							onTabChange={e => setTabMenuActiveItem(e.value)}
						/>
						{tabData.map((data, i) => (
							<DataFlowList {...data} key={i} />
						))}
					</div>
				</div>
			</div>
		</MainLayout>
	);
};
export default DataFlowTasks;
