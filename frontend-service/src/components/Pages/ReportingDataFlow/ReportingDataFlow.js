import React, { useState, useEffect, useContext } from "react";
import { BreadCrumb } from "primereact/breadcrumb";
import { Button } from "primereact/button";
import { SplitButton } from "primereact/splitbutton";
import { SplitButtonNE } from "../../Layout/UI/SplitButtonNE/SplitButtonNE";
import jsonDataSchema from "../../../assets/jsons/datosDataSchema3.json";
//import jsonDataSchemaErrors from '../../../assets/jsons/errorsDataSchema.json';
//import HTTPRequesterAPI from '../../../services/HTTPRequester/HTTPRequester';
import styles from "./ReportingDataFlow.module.css";
import ResourcesContext from "../../Context/ResourcesContext";
import MainLayout from "../../Layout/main-layout.component";
import DataFlowColumn from "../../Layout/UI/DataFlowColumn/DataFlowColumn";
import IconComponent from "../../Layout/UI/icon-component";
import config from "../../../conf";

const ReportingDataFlow = () => {
	const resources = useContext(ResourcesContext);
	const [breadCrumbItems, setBreadCrumbItems] = useState([]);

	console.log("ReportingDataFlow Render...");

	const home = { icon: resources.icons["home"], url: "#" };

	useEffect(() => {
		console.log("ReportingDataFlow useEffect");

		setBreadCrumbItems([
			{ label: resources.messages["AcceptedDF"], url: "#" },
			{ label: resources.messages["DFReporting"], url: "#" }
		]);
	}, [resources.messages]);

	let items = [
		{ label: "New", icon: "pi pi-fw pi-plus" },
		{ label: "Delete", icon: "pi pi-fw pi-trash" }
	];

	const { nameDataSetSchema } = jsonDataSchema;

	return (
		<div>
			<MainLayout>
				<div className="titleDiv">
					<BreadCrumb model={breadCrumbItems} home={home} />
				</div>
				<div className="rep-container">
					<div className="rep-row">
						<DataFlowColumn
							navTitle="data flows"
							dataFlowTitle={nameDataSetSchema}
						/>
						<div className={`${styles.pageContent} rep-col-12 rep-col-sm-8`}>
							<h2 className={styles.title}>
								<IconComponent icon={config.icons.shoppingCart} />
								{nameDataSetSchema}
							</h2>

							<div className={`${styles.buttonsWrapper}`}>
								<div>
									<Button label="DO" className="p-button-warning" />
									<p className="caption">Documents</p>
								</div>
								<div>
									<SplitButtonNE label="NE" className="p-button-primary" />
									<p className="caption">New dataset</p>
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
