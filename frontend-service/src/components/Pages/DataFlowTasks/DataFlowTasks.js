import React, { useEffect, useContext, useState } from "react";

import ResourcesContext from "../../Context/ResourcesContext";

// import DataFlaws from "../../../assets/jsons/DataFlaws2.json";

import styles from "./DataFlowTasks.module.scss";

import { BreadCrumb } from "primereact/breadcrumb";
import MainLayout from "../../Layout/main-layout.component";
import DataFlowColumn from "../../Layout/UI/DataFlowColumn/DataFlowColumn";
import { TabMenu } from "primereact/tabmenu";

import DataFlowList from "./DataFlowList/DataFlowList";

import HTTPRequesterAPI from "../../../services/HTTPRequester/HTTPRequester";
import config from "../../../conf";

//example of a namespace to messages keys
const i18nKey = "app.components.pages.dataFlowTasks";

const DataFlowTasks = ({ match, history }) => {
	const resources = useContext(ResourcesContext);

	const [breadCrumbItems, setBreadCrumbItems] = useState([]);
	const [tabMenuItems, setTabMenuItems] = useState([
		{
			label: resources.messages["dataFlowAcceptedPendingTab"],
			className: styles.flow_tab,
			tabKey: "pending"
		},
		{
			label: resources.messages["dataFlowCompletedTab"],
			className: styles.flow_tab,
			disabled: true,
			tabKey: "completed"
		}
	]);
	const [tabMenuActiveItem, setTabMenuActiveItem] = useState(tabMenuItems[0]);
	const [tabData, setTabData] = useState([]);
	const home = {
		icon: resources.icons["home"],
		command: () => history.push("/")
	};

	const dataFetch = () => {
		const c = {
			listKeys: [],
			apiUrl: "",
			userId: 2, //TODO HARDCODED userId,
			queryString: {}
		};
		if (tabMenuActiveItem.tabKey === "pending") {
			c.listKeys.push("pending");
			c.listKeys.push("accepted");
			c.apiUrl = `${config.loadDataFlowTaskPendingAcceptedAPI.url}${c.userId}`;
			c.queryString = {};
		} else {
			c.listKeys.push("completed");
			c.apiUrl = "";
		}

		HTTPRequesterAPI.get({
			url: c.apiUrl,
			queryString: c.queryString
		})
			.then(response => {
				//TODO STATUS HANDLING
				setTabData(
					c.listKeys.map(key => {
						return {
							listContent: response.data.filter(
								data => data.userRequestStatus.toLowerCase() === key
							),
							listType: key,
							listTitle: resources.messages[`${key}DataFlowTitle`],
							listDescription: resources.messages[`${key}DataFlowText`]
						};
					})
				);
			})
			.catch(error => {
				console.log("error", error);
				return error;
			});
	};

	useEffect(() => {
		console.log("Yahoooo! Rerendered");
		dataFetch();
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [resources.messages, tabMenuActiveItem]);

	//Bread Crumbs settings
	useEffect(() => {
		setBreadCrumbItems([{ label: resources.messages["dataFlowTask"] }]);
	}, [history, match.params.dataFlowId, resources.messages]);

	return (
		<MainLayout>
			<BreadCrumb model={breadCrumbItems} home={home} />
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
							<DataFlowList {...data} key={i} dataFetch={dataFetch} />
							//TODO completed pagination
						))}
					</div>
				</div>
			</div>
		</MainLayout>
	);
};
export default DataFlowTasks;
