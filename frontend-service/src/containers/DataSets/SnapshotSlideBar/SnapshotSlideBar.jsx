import React from "react";
import { Sidebar } from "primereact/sidebar";

import styles from "./SnapshotSliderBar.module.css";

const SnapshotSlideBar = ({ isVisible, setIsVisible }) => {
	return (
		<Sidebar visible={isVisible} onHide={e => setIsVisible()} position="right">
			<div className={styles.content}>
				<div className={styles.title}>
					<h3>Snapshots</h3>
				</div>
				<div className={`${styles.newContainer} ${styles.section}`}>
					<input type="text" placeholder="description" />
					<button>Create</button>
				</div>
				<hr />
				<div className={`${styles.listContainer}  ${styles.section}`}>
					<ul>
						<li className={styles.listItem}>
							<div className={styles.listItemData}>
								<h4>2019-07-03</h4>
								<p>fasñdkl jafsdñfklj asdñflkja sdñflkja</p>
							</div>
							<div className={styles.listActions}>
								<button>restore</button>
								<button>delete</button>
							</div>
						</li>
					</ul>
				</div>
			</div>
		</Sidebar>
	);
};

export default SnapshotSlideBar;
