import java.util.HashSet;
import java.util.Set;

public class Room {
    private static int id_generator = 0;

    private int id;
    private String name;
    private String town;
    private User root;
    private Set<User> users;
    private ImageLoader loader;
    private int current_round;
    private int rounds = 3;

    public Room(String name, User user){
        id = id_generator++;
        this.root = user;
        this.name = name;
        users = new HashSet<>();
        addUser(user);
        user.setRoom_id(id);
    }

    public void startRoom(){
        System.out.println("Игра в комнате " + this + " началась");
        current_round = 0;
        for (User user: users){
            user.startGameInRoom();
        }
        newRound();
    }

    public void newRound(){
        this.town = Main.getRandomTown();
        loader = new ImageLoader(town, 5);
        for (User user: users){
            user.startRoundInRoom(loader.clone(),current_round, town);
        }
    }

    public void checkEndRound(){
        for (User user: users){
            if (!user.isEnd_round()) return;
        }
        if(++current_round < rounds){
            newRound();
        }else{
            StringBuilder builder = new StringBuilder();
            builder.append("Игра оконыена! Результаты:\n");
            for (User user: users){
                builder.append(user.getName() + ": " + user.getPoints()+ "\n");
            }
            String ans = builder.toString();
            for (User user: users){
                user.end_room_game(ans);
            }
        }
    }


    public void removeAllUsers(){
        for (User user: users){
            removeUser(user);
        }
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
        user.setRoom_id(id);
    }

    public void removeUser(User user){
        users.remove(user);
        user.setRoom_id(-1);
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
