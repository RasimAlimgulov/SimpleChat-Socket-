package chat.client;

import java.util.HashSet;
import java.util.Set;

public class ClientGuiModel
{
    private final Set<String> allUserNames=new HashSet<>();
    private String newMessage;
    public void addUser(String newUserName){
          allUserNames.add(newUserName);
    }
    public void deleteUser(String userName){
        allUserNames.remove(userName);
    }

    public Set<String> getAllUserNames() {
        return allUserNames;
    }

    public String getNewMessage() {
        return newMessage;
    }

    public void setNewMessage(String newMessage) {
        this.newMessage = newMessage;
    }
}
