package dev.failapp.lab.repository;

import dev.failapp.lab.model.User;
import io.quarkus.hibernate.orm.panache.PanacheRepository;

import javax.enterprise.context.ApplicationScoped;
import java.util.Optional;

@ApplicationScoped
public class UserRepository implements PanacheRepository<User> {

    public Optional<User> findByDocumentId(String documentId) {
        return find("documentId", documentId).firstResultOptional();
    }

    public Optional<User> findByEmail(String email) {
        return find("emailAddress", email).firstResultOptional();
    }

}
