package org.eea.dataset.persistence.metabase.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Table(name = "RELEASE_RECEIPT")
public class ReleaseReceipt {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "release_receipt_id_seq")
    @SequenceGenerator(name = "release_receipt_id_seq", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "DATAFLOW_ID", nullable = false)
    private Long dataflowId;

    @Column(name = "USER_CUSTOM_TEXT")
    private String userCustomText;

    @Column(name = "UPDATED_AT", nullable = false)
    private LocalDateTime updatedAt;

    @Transient
    private String additionalMetadata;

    public ReleaseReceipt(Long dataflowId, String userCustomText, LocalDateTime updatedAt, String userEmail) {
        this.dataflowId = dataflowId;
        this.userCustomText = userCustomText;
        this.updatedAt = updatedAt;
    }
}
