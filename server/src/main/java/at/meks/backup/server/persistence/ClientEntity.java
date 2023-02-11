package at.meks.backup.server.persistence;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CLIENTS")
@Getter
@Setter
@Accessors(fluent = true, chain = true)
public class ClientEntity extends PanacheEntityBase {

    @Id
    @Column(name = "ID")
    private String id;
    @Column(name = "NAME")
    private String name;

}
