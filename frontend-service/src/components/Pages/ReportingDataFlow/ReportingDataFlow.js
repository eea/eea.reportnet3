import React, { useState, useEffect, useContext } from "react";

/* import jsonDataSchema from "../../../assets/jsons/datosDataSchema3.json"; */
//import jsonDataSchemaErrors from '../../../assets/jsons/errorsDataSchema.json';
import HTTPRequesterAPI from "../../../services/HTTPRequester/HTTPRequester";
import styles from "./ReportingDataFlow.module.css";
import ResourcesContext from "../../Context/ResourcesContext";

import { BreadCrumb } from "primereact/breadcrumb";
import { Button } from "primereact/button";
import { SplitButton } from "primereact/splitbutton";
import primeIcons from "../../../conf/prime.icons";
import MainLayout from "../../Layout/main-layout.component";
import DataFlowColumn from "../../Layout/UI/DataFlowColumn/DataFlowColumn";
import IconComponent from "../../Layout/UI/icon-component";
import { ProgressSpinner } from "primereact/progressspinner";

import config from "../../../conf/web.config.json";

const ReportingDataFlow = ({ history, match }) => {
	const resources = useContext(ResourcesContext);
	const [breadCrumbItems, setBreadCrumbItems] = useState([]);
	const [redirect, setRedirect] = useState(false);
	const [redirectTo, setRedirectTo] = useState("");
	const [splitOptions, setSplitOptions] = useState([]);
	const [dataFlowData, setDataFlowData] = useState(null);
	const [loading, setLoading] = useState(true);

	const home = {
		icon: resources.icons["home"],
		command: () => history.push("/")
	};

	useEffect(() => {
		HTTPRequesterAPI.get({
			url: `${config.loadDatasetsByDataflowID.url}${match.params.dataFlowId}`,
			queryString: {}
		})
			.then(response => {
				setDataFlowData(response.data);
				setLoading(false);
			})
			.catch(error => {
				setLoading(false);
				console.log("error", error);
				return error;
			});
	}, []);

	//Bread Crumbs settings
	useEffect(() => {
		setBreadCrumbItems([
			{
				label: resources.messages["dataFlowTask"],
				command: () => history.push("/data-flow-task")
			},
			{
				label: resources.messages["reportingDataFlow"]
			}
		]);
	}, [history, match.params.dataFlowId, resources.messages]);

	const handleRedirect = target => {
		history.push(target);
	};

	const layout = children => {
		return (
			<MainLayout>
				<div className="titleDiv">
					<BreadCrumb model={breadCrumbItems} home={home} />
				</div>
				<div className="rep-container">{children}</div>
			</MainLayout>
		);
	};

	if (loading) {
		return layout(<ProgressSpinner />);
	}

	return layout(
		<div className="rep-row">
			<DataFlowColumn
				navTitle={resources.messages["dataFlow"]}
				dataFlowTitle={dataFlowData.name}
				search={true}
			/>
			<div className={`${styles.pageContent} rep-col-12 rep-col-sm-9`}>
				<h2 className={styles.title}>
					<IconComponent icon={resources.icons["shoppingCart"]} />
					{dataFlowData.name}
				</h2>

				<div className={`${styles.buttonsWrapper}`}>
					<div className={styles.splitButtonWrapper}>
						<div className={`${styles.dataSetItem}`}>
							<Button
								label={resources.messages["do"]}
								className="p-button-warning"
								onClick={e => {
									handleRedirect(
										`/reporting-data-flow/${
											match.params.dataFlowId
										}/documentation-data-set/`
									);
								}}
							/>
							<p className={styles.caption}>
								{resources.messages["documents"]}
							</p>
						</div>
						{dataFlowData.datasets.map(item => {
							return (
								<div className={`${styles.dataSetItem}`} key={item.id}>
									<SplitButton
										label={resources.messages["ds"]}
										model={[
											{
												label: resources.messages["releaseDataCollection"],
												icon: primeIcons.icons.archive
											},
											{
												label: resources.messages["importFromFile"],
												icon: primeIcons.icons.import
											},
											{
												label: resources.messages["duplicate"],
												icon: primeIcons.icons.clone
											},
											{
												label: resources.messages["properties"],
												icon: primeIcons.icons.info
											}
										]}
										onClick={e => {
											handleRedirect(
												`/reporting-data-flow/${
													match.params.dataFlowId
												}/reporter-data-set/${item.id}`
											);
										}}
									/>
									<p className={styles.caption}>{item.dataSetName}</p>
								</div>
							);
						})}
					</div>
				</div>
			</div>
		</div>
	);
};

export default ReportingDataFlow;
