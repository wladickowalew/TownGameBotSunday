public class User {

    private String town;
    private ImageLoader loader;
    private String command;

    public User(){
        command = "begin";
    }

    public void startGame(String town){
        this.town = town;
        loader = new ImageLoader(town, 5);
    }

    public boolean isEnd(){
        return loader.isEmpty();
    }

    public String getImageURL(){
        return loader.getNextImageURL();
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
