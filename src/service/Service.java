/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import app.MessageType;
import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.Configuration;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Icon;
import javax.swing.JTextArea;
import model.Model_Client;
import model.Model_File;
import model.Model_Login;
import model.Model_Message;
import model.Model_MessageDB;
import model.Model_Package_Sender;
import model.Model_Receive_Image;
import model.Model_Receive_Message;
import model.Model_Register;
import model.Model_Send_Message;
import model.Model_User_Account;
import model.Model_Voice_Sender;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Admin
 */
public class Service {

    
    private static Service instance;
    private SocketIOServer server;
    private JTextArea textArea;
    private final int PORT_NUMBER = 1000;
    private ServiceUser serviceUser;
    private ServiceFile serviceFile;
    private List<Model_Client> listClient;
    
    public static Service getInstance(JTextArea textArea){
     if (instance == null) {
            instance = new Service(textArea);
        }
        return instance;
    }
    private Service(JTextArea textArea){
        this.textArea = textArea;
        this.serviceUser = new ServiceUser();
        serviceFile = new ServiceFile();
        listClient = new ArrayList<>();
        ByteOutputStream = new ByteArrayOutputStream();
    }
    public void startServer(){
         File f = new File("server_data");
        for (File fs : f.listFiles()) {
            fs.delete();
        }
        
        
        Configuration config = new Configuration();
        config.setPort(PORT_NUMBER);//cấu hình cổng 1000 cho server lắng nghe
        server = new SocketIOServer(config);
//        startServerSocket();
        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient sioc) {
                String clientId = sioc.getSessionId().toString(); // Lấy ID của client
        textArea.append("1 cliented. ID: " + clientId + "\n");
               }
        });
        server.addEventListener("register", Model_Register.class, new DataListener<Model_Register>() { //lắng nghe event register từ client
            @Override
            public void onData(SocketIOClient socketClient, Model_Register t, AckRequest ar) throws Exception {
                Model_Message message = serviceUser.register(t);
                ar.sendAckData(message.isAction(), message.getMessage(), message.getData());// phản hồi lại client với gói tin, lớp ackRequest từ socketIO
                if(message.isAction()){
                   textArea.append("User đã đăng ký với tài khoản :"+t.getUserName()+" Mật khẩu :"+t.getPassword()+"\n");
                   server.getBroadcastOperations().sendEvent("list_user", (Model_User_Account)message.getData());
                   addClient(socketClient, (Model_User_Account)message.getData());
                }
             }
        });
        
        server.addEventListener("login", Model_Login.class, new DataListener<Model_Login>() {//lắng nghe event login từ client
            @Override
            public void onData(SocketIOClient socketClient, Model_Login t, AckRequest ar) throws Exception {
                Model_User_Account login = serviceUser.login(t);
                if(login != null){
                   ar.sendAckData(true, login);
//                   login.setImage(null);
                    addClient(socketClient, login);
                    userConnect(login);
                } else{
                   ar.sendAckData(false);
                
                }
               }
        });
        
        server.addEventListener("list_user", Integer.class, new DataListener<Integer>() {//lắng nghe event list_user từ client
            @Override
            public void onData(SocketIOClient socketClient, Integer userID, AckRequest ar) throws Exception {
                try {
                    List<Model_User_Account> list = serviceUser.getUser(userID);// lấy list all user có trong db trừ userID này ra
                    socketClient.sendEvent("list_user", list.toArray());//phản hồi về lại client qua socketIOClient
                    
                } catch (SQLException e) {
                    System.err.println(e);
                }
               }
        });
        server.addDisconnectListener(new DisconnectListener() {
            @Override
            public void onDisconnect(SocketIOClient socketClient) {
                int userID = removeClient(socketClient);
                if(userID != 0){
                   //removed
                    userDisconnect(userID);
                }
                
               }
        });
        server.addEventListener("send_to_user", Model_Send_Message.class, new DataListener<Model_Send_Message>() {
            @Override
            public void onData(SocketIOClient socketClient, Model_Send_Message t, AckRequest ar) throws Exception {
                sendToClient(t, ar);
                
               }
        });
        
        server.addEventListener("send_file", Model_Package_Sender.class, new DataListener<Model_Package_Sender>() {
            @Override
            public void onData(SocketIOClient socketClient, Model_Package_Sender t, AckRequest ar) throws IOException {
                try {
                    
                    serviceFile.receiveFile(t);   
                    if(t.isFinish()){
                        ar.sendAckData(true);
                        Model_Receive_Image dataImage = new Model_Receive_Image();
                        dataImage.setFileID(t.getFileID());
                        dataImage.setFileName(t.getFileName());
                        Model_Send_Message message = serviceFile.closeFile(dataImage);
                        //gửi đến client 'message'
                        sendImgFileToClient(message, dataImage);
                        
                        
                    }else{
                        ar.sendAckData(true);
                    
                    }
                } catch (IOException | SQLException e) {
                    ar.sendAckData(false);
                    e.printStackTrace();
                }
                
                }
        });
        server.addEventListener("download", Model_Package_Sender.class, new DataListener<Model_Package_Sender>() {
            @Override
            public void onData(SocketIOClient socketClient, Model_Package_Sender t, AckRequest ar) throws Exception {
                    serviceFile.getFile(t);
                    socketClient.sendEvent("GetFile", new Model_Package_Sender(t.getFileID(), t.getFileName(), t.getData(), t.isFinish()));
                }
        } );
         server.addEventListener("send_voice", Model_Package_Sender.class, new DataListener<Model_Package_Sender>() {
            @Override
            public void onData(SocketIOClient socketClient, Model_Package_Sender t, AckRequest ar) throws Exception {
                 try {  
               
                     serviceFile.receiveVoice(t, ByteOutputStream); 
                    if(t.isFinish()){
                       ar.sendAckData(true);
                       byte[] dataVoice = ByteOutputStream.toByteArray();
                       System.out.println("độ dài data mic"+dataVoice.length);
                       sendVoiceToClient(t, dataVoice);
                       ByteOutputStream.reset();

                    }else{
                        ar.sendAckData(true);
                    
                    }
                } catch (IOException e) {
                    ar.sendAckData(false);
                    e.printStackTrace();
                }
                }
        }); 
         server.addEventListener("send_avatar", Model_Package_Sender.class, new DataListener<Model_Package_Sender>() {
            @Override
            public void onData(SocketIOClient socketClient, Model_Package_Sender t, AckRequest ar) throws Exception {
                try {
                     if(!t.isFinish()){
                       ByteOutputStream.write(t.getData());
                       ar.sendAckData(true);
                    }else{
                       ByteOutputStream.close();
                       ar.sendAckData(true);
                       byte[] data = ByteOutputStream.toByteArray();
                       System.out.println("độ dài data Avatar"+data.length);
                       serviceFile.updateAvatarData(data, t.getFromUserID());
                       ByteOutputStream.reset();
                       // next update send broadcast method to all client online at the moment
                   }
                  } catch (Exception e) {
                      ar.sendAckData(false);
                      e.printStackTrace();
                }
             
             
            }
        });
          server.addEventListener("load_messages", String.class, new DataListener<String>() {
    @Override
    public void onData(SocketIOClient socketClient, String jsonString, AckRequest ackRequest) throws Exception {
        try {
            JSONObject data = new JSONObject(jsonString); // Parse JSON String thành JSONObject
            int senderId = data.getInt("senderId");
            int receiverId = data.getInt("receiverId");
            System.out.println(data.toString());
            // Thực hiện xử lý tiếp...
            List<Model_MessageDB> messages = serviceUser.getMessages(senderId, receiverId);
            for (Model_MessageDB message : messages) {
    System.out.println("Message: " + message.toString());
}         
        Set<Model_MessageDB> uniqueMessages = new HashSet<>(messages);
        // Tạo lại JSONArray mới mỗi lần nhận tin nhắn
            JSONArray messageArray = new JSONArray();

            // Chuyển đổi danh sách tin nhắn thành JSON
            for (Model_MessageDB message : messages) {
                JSONObject messageJson = new JSONObject();
                messageJson.put("senderId", message.getIdsender());
                messageJson.put("receiverId", message.getIdreceiver());
                messageJson.put("content", message.getContent());
                messageJson.put("timestamp", message.getTimestamp());
                messageArray.put(messageJson);
            }

            // Gửi dữ liệu về client
            socketClient.sendEvent("receive_messages", messageArray.toString());

            // Log tin nhắn gửi đi
            System.out.println("Messages sent: " + messageArray.toString());
        } catch (JSONException e) {
            System.err.println("JSON Parsing Error: " + e.getMessage());
        }
    }
});




        server.start();
        textArea.append("Server starts initializing on port : "+PORT_NUMBER+"\n");
    }
    
    private void sendToClient(Model_Send_Message data, AckRequest ar){
        if(data.getMessageType() == MessageType.IMAGE.getValue()||data.getMessageType() == MessageType.FILE.getValue()) {
            try {
                Model_File file = serviceFile.addFileReceiver(data.getText()); //put lên csdl sql và trả về 1 instance model_file với id file và phần đuôi mở rộng
                serviceFile.initFile(file, data);// tạo file mới null
                ar.sendAckData(file.getFileID());//trả về id file cho client
               
                 if(data.getMessageType() == MessageType.IMAGE.getValue()){
//                     serviceUser.saveMessage(data.getFromUserID(), data.getToUserID(), null, file);
                          for(Model_Client c:listClient){
                          if(c.getUser().getUserID() == data.getToUserID()){
                          c.getClient().sendEvent("receive_data", new Model_File(file.getFileID(), file.getFileExtensions()));
                          break;
                      }
                   }
                }else if(data.getMessageType() == MessageType.FILE.getValue()){
                           
               }
           
            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        
        }else if(data.getMessageType() == MessageType.AVATAR.getValue()){
            try {
                
                //next update
              
                
            } catch (Exception e) {
                 e.printStackTrace();
            }
        }else{  
            serviceUser.saveMessage(data.getFromUserID(), data.getToUserID(), data.getText(), null);
             for(Model_Client c:listClient){
            if(c.getUser().getUserID() == data.getToUserID()){
               c.getClient().sendEvent("receive_ms", new Model_Receive_Message(data.getMessageType(), data.getFromUserID(), data.getText(), null));
               break;
            }
        
        } 
        }
        
       
    
    }
    private void sendImgFileToClient(Model_Send_Message data, Model_Receive_Image dataImage){
        
             try {
                 serviceUser.saveMessage(data.getFromUserID(), data.getToUserID(), data.getText(), dataImage.getFileName());
                 
            for(Model_Client c:listClient){
            if(c.getUser().getUserID() == data.getToUserID()){
               c.getClient().sendEvent("receive_ms", new Model_Receive_Message(data.getMessageType(), data.getFromUserID(), data.getText(), dataImage));           
               break;
            }
            System.out.println("a"+dataImage.getFileName());
        
        } 
        } catch (Exception e) {
                 System.err.println(e);
        }
    }
    
    private void sendVoiceToClient(Model_Package_Sender data, byte[] dataVoice){
             try {
            for(Model_Client c:listClient){
            if(c.getUser().getUserID() == data.getFileID()){
               c.getClient().sendEvent("receive_voice", new Model_Voice_Sender(data.getFileID(), data.getFromUserID(), dataVoice, data.getFileName()));           
               break;
            }
        
        } 
        } catch (Exception e) {
                 System.err.println(e);
        }
    }
    private void userConnect(Model_User_Account data){
            server.getBroadcastOperations().sendEvent("user_status", data, true);
    }
    private void userDisconnect(int userID){
            server.getBroadcastOperations().sendEvent("user_status", userID, false);
    }
    private void addClient(SocketIOClient socketClient, Model_User_Account user){
            listClient.add(new Model_Client(socketClient, user));
    }
    public int removeClient(SocketIOClient client){
            for(Model_Client d:listClient){
                if(d.getClient() == client){
                     listClient.remove(d);
                     return d.getUser().getUserID();
                }
            }
            return 0;
    }
        /**
     * @return the listClient
     */
    public List<Model_Client> getListClient() {
        return listClient;
    }
    
      /**
     * @return the outputStream
     */
    public ByteArrayOutputStream getOutputStream() {
        return ByteOutputStream;
    }
    
    

    
    private ServerSocket serverSocket;
    private Thread run;
    private ByteArrayOutputStream ByteOutputStream;
}
