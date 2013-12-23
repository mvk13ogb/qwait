package se.kth.csc.persist;

import se.kth.csc.model.Account;

public interface AccountStore {
    public Account fetchAccountWithId(int id);

    public Account fetchAccountWithPrincipalName(String principalName);

    public Account fetchNewestAccount();

    public void storeAccount(Account account);
}
