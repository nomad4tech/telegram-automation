package tech.nomad4.login.impl;

import tech.nomad4.login.decl.CodeProvider;

import java.util.Scanner;

class CodeProviderCLI implements CodeProvider {
    private final Scanner scanner = new Scanner(System.in);
    @Override
    public String getCode(String phone) {
        System.out.printf("Enter code for %s:", phone);
        return scanner.nextLine();
    }
}
