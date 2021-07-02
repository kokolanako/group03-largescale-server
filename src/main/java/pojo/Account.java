package pojo;

import lombok.Data;

import java.util.List;

@Data
public class Account {
    private String iban;
    private String name;
    private List<String> roles;

}
