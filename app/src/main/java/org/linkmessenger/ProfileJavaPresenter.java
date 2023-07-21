package org.linkmessenger;

import org.linkmessenger.profile.repository.ProfileRepository;

public class ProfileJavaPresenter {
    public ProfileRepository repository;

    public ProfileJavaPresenter(ProfileRepository repository) {
        this.repository = repository;
    }
}
