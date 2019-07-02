import React, { useState, useEffect, useContext } from "react";
import { BreadCrumb } from "primereact/breadcrumb";
import { Button } from "primereact/button";
import { SplitButton } from "primereact/splitbutton";
import jsonDataSchema from "../../../assets/jsons/datosDataSchema3.json";
//import jsonDataSchemaErrors from '../../../assets/jsons/errorsDataSchema.json';
//import HTTPRequesterAPI from '../../../services/HTTPRequester/HTTPRequester';
import styles from "./ReportingDataFlow.module.css";
import ResourcesContext from "../../Context/ResourcesContext";
import MainLayout from "../../Layout/main-layout.component";
import DataFlowColumn from "../../Layout/UI/DataFlowColumn/DataFlowColumn";
import IconComponent from "../../Layout/UI/icon-component";
import config from "../../../conf";

const ReportingDataFlow = ({ history, match }) => {
	const resources = useContext(ResourcesContext);
	/* const [breadCrumbItems, setBreadCrumbItems] = useState([]); */
	const [redirect, setRedirect] = useState(false);
	const [redirectTo, setRedirectTo] = useState("");
	const [splitOptions, setSplitOptions] = useState([]);

	const home = { icon: resources.icons["home"], url: "/" };

	/* useEffect(() => {
		console.log("ReportingDataFlow useEffect");

		setBreadCrumbItems([
			{ label: resources.messages["AcceptedDF"], url: "#" },
			{ label: resources.messages["DFReporting"], url: "#" }
		]);
	}, [resources.messages]); */

	/* let items = [
		{ label: "New", icon: "pi pi-fw pi-plus" },
		{ label: "Delete", icon: "pi pi-fw pi-trash" }
	]; */

	useEffect(() => {
		setSplitOptions([
			{
				label: "Release to data collection",
				icon: "pi pi-unlock"
			},
			{
				label: "Import from file",
				icon: "pi pi-upload"
			},
			{
				label: "Duplicate",
				icon: "pi pi-copy"
			},
			{
				label: "Properties",
				icon: "pi pi-info-circle"
			}
		]);
	}, [])

	const { nameDataSetSchema } = jsonDataSchema;

	const handleRedirect = target => {
		history.push(target);
	};

	return (
		<div>
			<MainLayout>
				<div className="titleDiv">
					<BreadCrumb
						model={[
							{ label: resources.messages["reportingDataFlow"], url: "#" }
						]}
						home={home}
					/>
				</div>
				<div className="rep-container">
					<div className="rep-row">
						<DataFlowColumn
							navTitle={resources.messages["dataFlow"]}
							dataFlowTitle={nameDataSetSchema}
							search={true}
						/>
						<div className={`${styles.pageContent} rep-col-12 rep-col-sm-9`}>
							<h2 className={styles.title}>
								<IconComponent icon={config.icons.shoppingCart} />
								{nameDataSetSchema}
							</h2>

							<div className={`${styles.buttonsWrapper}`}>
								<div>
									<Button
										label={resources.messages["do"]}
										className="p-button-warning"
										onClick={e => {
											handleRedirect(
												`/documentation-data-set/${match.params.id}`
											);
										}}
									/>
									<p className={styles.caption}>{resources.messages["documents"]}</p>
								</div>
								<div className={styles.buttonwrapper}>
									<SplitButton label={resources.messages['ds']} model={splitOptions} handleRedirect={handleRedirect} />
									<p className={styles.caption}>{resources.messages["dataSet"]}</p>
								</div>
							</div>
						</div>
					</div>
				</div>
			</MainLayout>
		</div>
	);
};

export default ReportingDataFlow;
