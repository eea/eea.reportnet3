import React, { useEffect, useContext, useState } from "react";

import ResourcesContext from "../../Context/ResourcesContext";

import DataFlaws from "../../../assets/jsons/DataFlaws2.json";

import styles from "./DataFlowTasks.module.scss";

import { BreadCrumb } from "primereact/breadcrumb";
import MainLayout from "../../Layout/main-layout.component";
import DataFlowColumn from "../../Layout/UI/DataFlowColumn/DataFlowColumn";
import { TabMenu } from "primereact/tabmenu";

import DataFlowList from "./DataFlowList/DataFlowList";

//example of a namespace to messages keys
const i18nKey = "app.components.pages.dataFlowTasks";

//import HTTPRequesterAPI from '../../../services/HTTPRequester/HTTPRequester';
//import ResourcesContext from '../../Context/ResourcesContext';

const DataFlowTasks = () => {
	const resources = useContext(ResourcesContext);

	const [tabMenuItems, setTabMenuItems] = useState([
		{
			label: "Pending / Accepted",
			className: styles.flow_tab,
			tabKey: "pending"
		},
		{ label: "completed", className: styles.flow_tab, tabKey: "completed" }
	]);
	const [tabMenuActiveItem, setTabMenuActiveItem] = useState(tabMenuItems[0]);
	const [tabData, setTabData] = useState([]);
	const home = { icon: resources.icons["home"], url: "/" };

	useEffect(() => {
		const result = DataFlaws;
		const listKeys = [];
		if (tabMenuActiveItem.tabKey === "pending") {
			listKeys.push("pending");
			listKeys.push("accepted");
		} else {
			listKeys.push("completed");
		}
		setTabData(
			listKeys.map(key => {
				return {
					listContent: result.filter(data => data.status.toLowerCase() === key),
					listType: key,
					listTitle: resources.messages[`${key}DataFlowTitle`],
					listDescription: resources.messages[`${key}DataFlowText`]
				};
			})
		);
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
							//TODO completed pagination
						))}
					</div>
				</div>
			</div>
		</MainLayout>
	);
};
export default DataFlowTasks;
