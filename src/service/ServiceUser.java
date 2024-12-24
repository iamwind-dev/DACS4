/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package service;

import java.sql.PreparedStatement;
import connection.DatabaseConnection;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.security.Timestamp;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import model.Model_Client;
import model.Model_Login;
import model.Model_Message;
import model.Model_MessageDB;
import model.Model_Receive_Image;
import model.Model_Register;
import model.Model_User_Account;
/**
 *
 * @author Admin
 */
public class ServiceUser {
    private final String PATH_FILE = "server_data/profile.png";
  //SQL truy vấn
    private final String LOGIN = "select UserID, user_account.UserName, Gender, Image from `user` join user_account using (UserID) where `user`.UserName=BINARY(?) and `user`.`Password`=BINARY(?) and user_account.`Status`='1'";
    private final String INSERT_USER = "insert into user (UserName, Password) values (?,?)";
    private final String INSERT_USER_ACCOUNT = "insert into user_account (UserID, UserName) values (?,?)";
    private final String CHECK_USER = "select UserID from user where UserName =? limit 1";
    // lấy info các user còn lại trừ userID này ra
    private final String SELECT_USER_ACCOUNT = "select UserID, UserName, Gender, Image from user_account where user_account.`Status`='1' and UserID<>?";
    //khởi tạo connect sql
    private final Connection con;
    public ServiceUser() {
        this.con = DatabaseConnection.getInstance().getConnection();
    }
//    public Model_Message register(Model_Register data) throws IOException{
//    //kiểm tra user tồn tại
//        
//        Model_Message message = new Model_Message();
//        try {
//        
//        File f = new File(PATH_FILE); 
//        System.out.println("test4");
////         byte[] dataAvatar = Files.readAllBytes(f.toPath());
//         System.out.println("test5");
//        PreparedStatement p = con.prepareStatement(CHECK_USER, 
//                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
//        System.out.println("test6");
//        p.setString(1, data.getUserName());
//        ResultSet r = p.executeQuery();
//         System.out.println("test3");
//        if(r.first()){  
//            System.out.println("test");
//             message.setAction(false);
//             message.setMessage("Người dùng đã tồn tạiiiii");   
//        }else{
//            System.out.println("test2");
//             message.setAction(true);
//        }
//        r.close();
//        p.close();
//        if(message.isAction()){
//         //insert user đang ký
//         con.setAutoCommit(false);
//         p = con.prepareStatement(INSERT_USER, PreparedStatement.RETURN_GENERATED_KEYS);
//         p.setString(1, data.getUserName());
//            System.out.println(data.getUserName());
//         p.setString(2, data.getPassword());
//         p.execute();
//         r = p.getGeneratedKeys();
//         r.first();
//         int userID = r.getInt(1);
//         r.close();
//         p.close();
//         //create avatar image for new user register
//       
//         FileInputStream fis = new FileInputStream(f.getAbsolutePath());
//         //tao user account
//         p = con.prepareStatement(INSERT_USER_ACCOUNT);
//         
//         p.setInt(1, userID);
//         p.setString(2, data.getUserName());
//         p.setBinaryStream(3, fis);
//         p.execute();
//         p.close();
//         con.commit(); //xác nhận commit thay đổi 
//         con.setAutoCommit(true);          
//             message.setAction(true);
//             message.setMessage("Đăng ký tài khoản thành công !"    );
////             message.setData(new Model_User_Account(userID, data.getUserName(), "", dataAvatar, true));//set list thông tin user để gửi về client hiển thị
//       
//       }
//        } catch (SQLException e) {
//            System.out.println(e);
//            message.setAction(false);
//            message.setMessage("Server error");
//            try {
//                if(con.getAutoCommit() == false){
//                    con.rollback();
//                    con.setAutoCommit(true);
//                
//                }
//            } catch (Exception e1) {
//            }
//            
//        }
//
//       
//       return message;
//    
//    }
    
    public Model_Message register(Model_Register data) {
        //  Check user exit
        Model_Message message = new Model_Message();
        try {
             PreparedStatement p = con.prepareStatement(CHECK_USER, 
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            p.setString(1, data.getUserName());
            ResultSet r = p.executeQuery();
            if (r.first()) {
                message.setAction(false);
                message.setMessage("User Already Exit");
            } else {
                message.setAction(true);
            }
            r.close();
            p.close();
            if (message.isAction()) {
                //  Insert User Register
                con.setAutoCommit(false);
                p = con.prepareStatement(INSERT_USER, PreparedStatement.RETURN_GENERATED_KEYS);
                p.setString(1, data.getUserName());
                p.setString(2, data.getPassword());
                p.execute();
                r = p.getGeneratedKeys();
                r.first();
                int userID = r.getInt(1);
                r.close();
                p.close();
                //  Create user account
                p = con.prepareStatement(INSERT_USER_ACCOUNT);
                p.setInt(1, userID);
                p.setString(2, data.getUserName());
                p.execute();
                p.close();
                con.commit();
                con.setAutoCommit(true);
                message.setAction(true);
                message.setMessage("Ok");
                message.setData(new Model_User_Account(userID, data.getUserName(), "", null, true));
            }
        } catch (SQLException e) {
            message.setAction(false);
            message.setMessage(e.getMessage());
            System.out.println(e.getMessage());
            try {
                if (con.getAutoCommit() == false) {
                    con.rollback();
                    con.setAutoCommit(true);
                }
            } catch (SQLException e1) {
            }
        }
        return message;
    }
    
    public Model_User_Account login(Model_Login login)throws SQLException, IOException{
        
    Model_User_Account data = null;
    PreparedStatement p = con.prepareStatement(LOGIN,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
    p.setString(1, login.getUserName());
    p.setString(2, login.getPassword());
    ResultSet r = p.executeQuery();
    try{
    if(r.first()){
        int userID = r.getInt(1);
        String userName = r.getString(2);
        String gender = r.getString(3);
//        InputStream input = r.getBinaryStream(4);
//        byte[] imageAvatar = new byte[input.available()];
//        imageAvatar = input.readAllBytes();
        data = new Model_User_Account(userID, userName, gender, "", true);
//        input.close();
        r.close();
    p.close();
    }
        } catch (SQLException e){
            System.out.println(e);
        }
    
    return data;
    
    }
    
    public List<Model_User_Account> getUser(int exitUser) throws SQLException, IOException {
              List<Model_User_Account> list = new ArrayList<>();
             PreparedStatement p = con.prepareStatement(SELECT_USER_ACCOUNT, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
              p.setInt(1, exitUser);
              ResultSet r = p.executeQuery();
              try{
              while(r.next()){
                  int userID = r.getInt(1);
                  String userName = r.getString(2);
                  String gender = r.getString(3);
//                  InputStream input = r.getBinaryStream(4);
//                  byte[] imageAvatar = new byte[input.available()];
//                  imageAvatar = input.readAllBytes();
                    System.out.println(userID);
                  list.add(new Model_User_Account(userID, userName, gender, "", checkUserStatus(userID)));
//                  input.close();
              }
              r.close();
              p.close();
              } catch (SQLException e){
            System.out.println(e);
        }
              return list;
    } 
   private boolean checkUserStatus(int userID){
       List<Model_Client> clients = Service.getInstance(null).getListClient();
       for(Model_Client c:clients ){
           System.out.println(c.getUser().getUserID()+"online");
             if (c.getUser().getUserID() == userID) {
               return true;
           }
       }
       return false;
   }
   
   public boolean saveMessage( int idsender, int idreceiver, String content, String file_path) {
        try {
            // Query thêm tin nhắn
            String query = "INSERT INTO messages (idsender, idreceiver, content, file_name, file_path) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement p = con.prepareStatement(query);
            p.setInt(1, idsender);
            p.setInt(2, idreceiver);
            p.setString(3, content);
            p.setString(4, file_path);
            p.setString(5, file_path);
            // Xử lý file
//            if (file != null && file.exists()) {
//                p.setString(4, file.getName());
//                p.setString(5, file.getAbsolutePath());
//            } else {
//                p.setNull(4, java.sql.Types.VARCHAR);
//                p.setNull(5, java.sql.Types.VARCHAR);
//            }

            // Thực thi lệnh
            p.executeUpdate();
            p.close();
            return true;
        } catch (SQLException e) {
            System.out.println("Error saving message: " + e.getMessage());
            return false;
        }
    }
   
   

    // Hàm lấy danh sách tin nhắn
    public List<Model_MessageDB> getMessages(int idsender, int idreceiver) {
    List<Model_MessageDB> messages = new ArrayList<>();
    try {
        // Query lấy tin nhắn hai chiều giữa idsender và idreceiver
        String query = "SELECT * FROM messages WHERE " +
                       "(idsender = ? AND idreceiver = ?) OR " +
                       "(idsender = ? AND idreceiver = ?) " +
                       "ORDER BY timestamp";
        PreparedStatement p = con.prepareStatement(query);
        // Truyền tham số
        p.setInt(1, idsender);
        p.setInt(2, idreceiver);
        p.setInt(3, idreceiver);
        p.setInt(4, idsender);
        
        // Thực thi lệnh và đọc kết quả
        ResultSet r = p.executeQuery();
        while (r.next()) {
            java.sql.Timestamp timestamp = r.getTimestamp("timestamp");
            
            // Chuyển thành định dạng chuỗi (giờ)
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            String formattedTime = sdf.format(timestamp);
            messages.add(new Model_MessageDB(
                r.getInt("idsender"),
                r.getInt("idreceiver"),
                r.getString("content"),
                formattedTime
            ));
        }
        r.close();
        p.close();
    } catch (SQLException e) {
        System.out.println("Error retrieving messages: " + e.getMessage());
    }
    return messages;
}

    
}
