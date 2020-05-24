import java.util.HashSet;
import java.util.Set;

public class Room {
    private static  int id_generator = 0;

    private int id;
    private String name;
    private String town;
    private User root;
    private Set<User> users;
    private ImageLoader loader;

    public Room(String name, User user){
        id = id_generator++;
        this.root = user;
        this.name = name;
        users = new HashSet<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public int getId() {
        return id;
    }

    public boolean isRoot(User user){
        return user.equals(root);
    }

    public void addUser(User user){
        users.add(user);
    }

    public void removeUser(User user){
        users.remove(user);
    }

    public String getUsers(){
        StringBuilder builder = new StringBuilder();
        for(User user: users){
            builder.append(user.toString() + "\n");
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return "id=" + id + ", name=" + name;
    }
}
