import React, { useContext } from "react";
import IconComponent from "../../../../Layout/UI/icon-component";
import primeIcons from "../../../../../conf/prime.icons";
import styles from "./DataFlowItem.module.scss";
import ResourcesContext from "../../../../Context/ResourcesContext";

const DataFlowItem = props => {

	const resources = useContext(ResourcesContext);

	const { itemContent, listType } = props;
	return (
		<div
			className={
				listType === "accepted" || listType === "completed"
					? `${styles.container} ${styles.accepted}`
					: `${styles.container}`
			}
		>
			<div className={`${styles.card_component_icon}`}>
				<IconComponent
					icon={`${primeIcons.icons.clone}`}
					className={`${styles.card_component_icon_i}`}
				/>
			</div>

			<div className={`${styles.card_component_content} `}>
				<div className={`${styles.card_component_content_date}`}>
					<span>{itemContent.date}</span>
				</div>
				<h3 className={`${styles.card_component_content_title}`}>
					{itemContent.title}
				</h3>

				<p>{itemContent.description}</p>
			</div>

			<div className={`${styles.card_component_btn}`}>
				{listType === "pending" ? (
					<>
						<button type="button" className={`${styles.rep_button}`}>
							{resources.messages['accept']}
						</button>

						<button type="button" className={`${styles.rep_button}`} disabled>
							{resources.messages['reject']}
						</button>
					</>
				) : (
					<>
						<IconComponent icon={`${primeIcons.icons.comment}`} />
						<IconComponent icon={`${primeIcons.icons.share}`} />
					</>
				)}
			</div>
		</div>
	);
};

export default DataFlowItem;
