package at.meks.backup.server.persistence.client;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "CLIENTS")
public class ClientEntity extends PanacheEntityBase {

    @Id
    @Column(name = "ID")
    public String id;
    @Column(name = "NAME")
    public String name;

}
