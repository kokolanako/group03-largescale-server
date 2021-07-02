package pojo;

import lombok.Data;

import java.util.List;

@Data
public class Organisation {
    private String name;
    private List<Role> roles;
    private List<Account> accounts;
}
