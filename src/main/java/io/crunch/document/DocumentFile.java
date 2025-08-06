package io.crunch.document;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.*;

import java.util.Optional;

@Entity
@Table(name = "DOCUMENT_FILE")
public class DocumentFile extends PanacheEntityBase {

    @Id
    @SequenceGenerator(name = "id_gen", sequenceName = "id_sequence", allocationSize = 1)
    @GeneratedValue(generator = "id_gen", strategy = GenerationType.SEQUENCE)
    private Long id;

    /** Unique name of the file. */
    @Column(name = "file_name", updatable = false, nullable = false)
    private String fileName;

    /** Contains the file checksum value produces by SHA-256 algorithm. */
    @Column(name = "checksum", updatable = false, nullable = false)
    private String checksum;

    public static Optional<DocumentFile> findByChecksum(String checksum) {
        return find("checksum", checksum).firstResultOptional();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }
}
