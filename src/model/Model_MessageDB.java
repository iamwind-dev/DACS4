/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.io.Serializable;
import java.security.Timestamp;

/**
 *
 * @author duong
 */
public class Model_MessageDB implements Serializable {
	int idsender;
	int idreceiver;
	String content;
	String file_name;
	String file_path;
        String timestamp;
        private Model_Receive_Image dataImage;
	public Model_MessageDB(int idsender, int idreceiver, String content, String file_name, String file_path) {
		super();
		this.idsender = idsender;
		this.idreceiver = idreceiver;
		this.content = content;
		this.file_name = file_name;
		this.file_path = file_path;
                
	}
        
        public Model_MessageDB(int idsender, int idreceiver, String content, String file_name, Model_Receive_Image dataImage) {
		super();
		this.idsender = idsender;
		this.idreceiver = idreceiver;
		this.content = content;
		this.file_name = file_name;
		this.file_path = file_path;
                this.dataImage = dataImage;
	}
	
	public Model_MessageDB(int idsender, String content, String file_name, String file_path) {
		super();
		this.idsender = idsender;
		this.content = content;
		this.file_name = file_name;
		this.file_path = file_path;
	}
        
        public Model_MessageDB(int idsender, int idreceiver) {
		super();
		this.idsender = idsender;
		this.idreceiver = idreceiver;    
	}

    public int getIdsender() {
        return idsender;
    }

    public void setIdsender(int idsender) {
        this.idsender = idsender;
    }

    public int getIdreceiver() {
        return idreceiver;
    }

    public void setIdreceiver(int idreceiver) {
        this.idreceiver = idreceiver;
    }

	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getFile_name() {
		return file_name;
	}
	public void setFile_name(String file_name) {
		this.file_name = file_name;
	}
	public String getFile_path() {
		return file_path;
	}
	public void setFile_path(String file_path) {
		this.file_path = file_path;
	}

    @Override
    public String toString() {
        return "Model_MessageDB{" + "idsender=" + idsender + ", idreceiver=" + idreceiver + ", content=" + content + ", file_name=" + file_name + ", file_path=" + file_path + ", dataImage=" + dataImage + '}';
    }

    public Model_MessageDB(int idsender, int idreceiver, String content) {
        this.idsender = idsender;
        this.idreceiver = idreceiver;
        this.content = content;
    }

    public Model_MessageDB(int idsender, int idreceiver, String content, String timestamp) {
        this.idsender = idsender;
        this.idreceiver = idreceiver;
        this.content = content;
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
        
}