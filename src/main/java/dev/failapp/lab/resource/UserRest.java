package dev.failapp.lab.resource;

import dev.failapp.lab.model.User;
import dev.failapp.lab.service.UserService;
import org.jboss.resteasy.annotations.jaxrs.PathParam;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Path("/")
public class UserRest {

    @Inject
    private UserService userService;

    @GET
    public Response index() {
        return Response.status(Response.Status.NOT_FOUND)
                .build();
    }

    @GET
    @Path("/api/v1/lab/users")
    public List<User> list(@QueryParam Integer page, @QueryParam Integer size, @QueryParam String order) {
        if (Optional.ofNullable(page).isEmpty()) page=1;
        if (Optional.ofNullable(size).isEmpty()) size=100;
        if (Optional.ofNullable(order).isEmpty()) order="desc";
        return userService.fetchUsers(page, size, order);
    }

    @GET
    @Path("/api/v1/lab/users/{documentId}")
    public Response findByDocumentId(@PathParam String documentId) {
        Optional<User> user = userService.fetchUser(documentId);
        if (user.isPresent())
            return Response.ok(user.get()).build();

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Path("/api/v1/lab/users")
    public Response save(User user) {

        Optional<User> _user = userService.saveUser(user);
        if (_user.isPresent())
            return Response.ok(_user.get()).build();

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @POST
    @Path("/api/v2/lab/users")
    public Response saveUsers(List<User> userList) {

        List<Map<String, Object>> mapList = userService.saveUserList(userList);
        if (mapList.size() > 0)
            return Response.ok(mapList).build();

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @DELETE
    @Path("/api/v1/lab/users/{documentId}")
    public Response delete(@PathParam String documentId) {

        boolean delete = userService.deleteUser(documentId);
        if (delete)
            return Response.ok().build();

        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @GET
    @Path("/api/v1/lab/users/count")
    public long count() {
        return userService.countUsers();
    }

}
