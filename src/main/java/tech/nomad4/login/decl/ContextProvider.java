package tech.nomad4.login.decl;

import java.util.Optional;

public interface ContextProvider {

    void updateContext(String phone, String context);

    Optional<String> getContext(String phone);

}
