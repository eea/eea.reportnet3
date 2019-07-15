import React, { useContext } from "react";
import { Link } from "react-router-dom";
import moment from "moment";

import IconComponent from "../../../../Layout/UI/icon-component";
import primeIcons from "../../../../../conf/prime.icons";

import styles from "./DataFlowItem.module.scss";
import ResourcesContext from "../../../../Context/ResourcesContext";
import HTTPRequesterAPI from "../../../../../services/HTTPRequester/HTTPRequester";

const DataFlowItem = props => {
	const resources = useContext(ResourcesContext);

	const { itemContent, listType, dataFetch } = props;

	const acceptDataFlow = () => {
		const dataPromise = HTTPRequesterAPI.update({
			url: `/dataflow/updateStatusRequest/${itemContent.id}?type=ACCEPTED`,
			data: {},
			queryString: {}
		});

		dataPromise
			.then(response => {
				//rerender DataFlowList component
				dataFetch();
				console.log(response);
			})
			.catch(error => {
				//TODO create dialog mesage with error text
				console.warn("ACCEPT ERROR =>  ", error);
				return error;
			});
	};

	const rejectDataFlow = () => {
		//TODO Call to DB
		const dataPromise = HTTPRequesterAPI.post({
			url: `/dataflow/updateStatusRequest/${itemContent.id}?type=REJECTED`,
			data: {},
			queryString: {}
		});

		dataPromise
			.then(response => {
				//TODO rerender DataFlowList component
				console.log(response);
			})
			.catch(error => {
				//TODO create dialog mesage with error text
				console.warn("REJECT ERROR => ", error);
				return error;
			});
	};

	const layout = children => {
		return (
			<div
				className={
					listType === "accepted" || listType === "completed"
						? `${styles.container} ${styles.accepted}`
						: `${styles.container}`
				}
			>
				{listType === "accepted" ? (
					<Link
						className={styles.containerLink}
						to={`/reporting-data-flow/${itemContent.id}`}
					>
						{children}
					</Link>
				) : (
					<>{children}</>
				)}
			</div>
		);
	};

	return layout(
		<>
			<div className={`${styles.card_component_icon}`}>
				<IconComponent
					icon={`${primeIcons.icons.clone}`}
					className={`${styles.card_component_icon_i}`}
				/>
			</div>

			<div className={`${styles.card_component_content} `}>
				<div className={`${styles.card_component_content_date}`}>
					<span>{moment(itemContent.deadlineDate).format("YYYY-MM-DD")}</span>
				</div>
				<h3 className={`${styles.card_component_content_title}`}>
					{itemContent.name}
				</h3>

				<p>{itemContent.description}</p>
			</div>

			<div className={`${styles.card_component_btn}`}>
				{listType === "pending" ? (
					<>
						<button
							type="button"
							className={`${styles.rep_button}`}
							onClick={() => acceptDataFlow()}
						>
							{resources.messages["accept"]}
						</button>

						<button
							type="button"
							className={`${styles.rep_button}`}
							/* disabled */ onClick={() => rejectDataFlow()}
						>
							{resources.messages["reject"]}
						</button>
					</>
				) : (
					<>
						<a className={styles.btn} href="#">
							<IconComponent icon={`${primeIcons.icons.comment}`} />
						</a>
						<a className={styles.btn} href="http://">
							<IconComponent icon={`${primeIcons.icons.share}`} />
						</a>
					</>
				)}
			</div>
		</>
	);
};

export default DataFlowItem;
