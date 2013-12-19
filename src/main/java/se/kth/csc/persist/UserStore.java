package se.kth.csc.persist;

import se.kth.csc.model.User;

public interface UserStore {
    public User fetchUserWithId(int id);

    public User fetchNewestUser();

    public void storeUser(User user);
}
