package app.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**

 */
@Entity
@Table(name = "roles")
@Getter
@Setter
@NamedQueries(@NamedQuery(name = "Role.deleteAllRows", query = "DELETE from Role"))
public class Role implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @Column(name = "role_name", length = 20)
    private String role;

    @Getter
    @ToString.Exclude
    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();

    public Role() {}

    public Role(String roleName) {
        this.role = roleName;
    }

    public String getRoleName() {
        return role;
    }

    @Override
    public String toString() {
        return "Role{" + "roleName='" + role + '\'' + '}';
    }
}
