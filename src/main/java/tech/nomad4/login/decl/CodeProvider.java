package tech.nomad4.login.decl;

@FunctionalInterface
public interface CodeProvider {
    String getCode(String phone);

}
