package dev.failapp.lab.service;

import dev.failapp.lab.model.User;
import dev.failapp.lab.repository.UserRepository;
import io.quarkus.cache.CacheInvalidate;
import io.quarkus.cache.CacheResult;
import io.quarkus.hibernate.orm.panache.PanacheQuery;
import io.quarkus.panache.common.Page;
import io.quarkus.panache.common.Sort;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.util.*;

@ApplicationScoped
public class UserService {

    private static final Logger log = Logger.getLogger(UserService.class);

    @Inject
    private UserRepository userRepository;

    public List<User> fetchUsers(int page) {
        int perPage = 100;
        page = page -1;
        PanacheQuery<User> users = userRepository.findAll(Sort.ascending("id"));
        users.page(Page.ofSize(perPage));
        return users.page( Page.of(page, perPage) ).list();
    }

    @CacheResult(cacheName = "cache-users")
    public Optional<User> fetchUser(String documentId) {
        if (Optional.ofNullable(documentId).isEmpty()) return Optional.empty();

        log.infof("[x] find entity by documentId: %s", documentId);
        return userRepository.findByDocumentId(documentId.strip());
    }

    public long countUsers() {
        return userRepository.count();
    }

    @Transactional
    public Optional<User> saveUser(User user) {
        if (!validateData(user)) return Optional.empty();
        String documentId = user.getDocumentId();
        Optional<User> _user = userRepository.findByDocumentId(documentId);
        if (_user.isEmpty()) {
            return this.addUser(user);
        } else {
            return this.updateUser(_user.get(), user);
        }
    }

    private Optional<User> addUser(User user) {

        try {
            userRepository.persist(user);
            return userRepository.findByDocumentId(user.getDocumentId());
        } catch (Exception ex) {
            log.errorf("[x] error: %s", ex.getMessage());
        }
        return Optional.empty();
    }

    private Optional<User> updateUser(User _user, User user) {

        try {
            _user.setFirstName( user.getFirstName().strip() );
            _user.setLastName( user.getLastName().strip() );
            _user.setGender( user.getGender() );
            _user.setEmailAddress( user.getEmailAddress().strip() );
            _user.setPhoneNumber( user.getPhoneNumber().strip() );
            userRepository.persist(_user);
            return Optional.of(_user);
        } catch (Exception ex) {
            log.errorf("[x] error: %s", ex.getMessage());
        }
        return Optional.empty();
    }

    @Transactional
    @CacheInvalidate(cacheName = "cache-users")
    public boolean deleteUser(String documentId) {

        if (Optional.ofNullable(documentId).isEmpty()) return false;

        Optional<User> user = userRepository.findByDocumentId(documentId.strip());
        if (user.isPresent()) {
            log.infof("[x] delete entity with documentId: %s", documentId);
            return userRepository.deleteById(user.get().getId());
        }
        return false;
    }


    private boolean validateData(User user) {
        if (Optional.ofNullable(user).isEmpty()) return false;

        if (Optional.ofNullable(user.getDocumentId()).isEmpty()) return false;
        if (Optional.ofNullable(user.getFirstName()).isEmpty()) return false;
        if (Optional.ofNullable(user.getLastName()).isEmpty()) return false;
        if (Optional.ofNullable(user.getEmailAddress()).isEmpty()) return false;
        if (Optional.ofNullable(user.getGender()).isEmpty()) return false;

        return true;
    }

    public List<Map<String, Object>> saveUserList(List<User> userList) {

        try {

            if (Optional.ofNullable(userList).isEmpty() || userList.size() == 0)
                return Collections.emptyList();

            List<Map<String, Object>> mapList = new ArrayList<>();

            userList.forEach( user -> {
                if (this.validateData(user)) {
                    Optional<User> _user = this.saveUser(user);
                    if (_user.isPresent()) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("documentId", _user.get().getDocumentId());
                        mapList.add(map);
                    }
                }
            });

            return mapList;

        } catch (Exception ex) {
            log.errorf("[x] error: %s", ex.getMessage());
        }

        return Collections.emptyList();
    }

}
