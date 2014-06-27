package models;

import java.util.*;
import play.modules.mongodb.jackson.MongoDB;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.Id;
import net.vz.mongodb.jackson.ObjectId;
import net.vz.mongodb.jackson.WriteResult;
import org.codehaus.jackson.annotate.JsonProperty;

import com.mongodb.BasicDBObject;

import javax.persistence.*;

public class User {
	
	@Id
	@ObjectId
	public String id;
	public String name;
	public String nickName;
	public String pwd;
	
	private static JacksonDBCollection<User, String> collection = MongoDB.getCollection( "users", User.class, String.class);
	
	public User() {
	}
	
	public User( String name, String nickName, String pwd) {
		this.name = name;
		this.nickName = nickName;
		this.pwd = pwd;
	}
	
	public static List<User> all() {
		return User.collection.find().toArray();
	}
	
	public static String create( User user) {
		BasicDBObject query = new BasicDBObject( "nickName", user.nickName);
		if ( User.collection.find( query).toArray().size() == 0 ) {
			WriteResult<User, String> insertion = User.collection.insert( user);
			return insertion.getSavedId();
		}
		else {
			return null;
		}
	}
	
	public static String create( String name, String nickName, String pwd) {
		BasicDBObject query = new BasicDBObject( "nickName", nickName);
		/*
		System.out.println( "Size = " + User.collection.find( query).toArray() );*/
		if ( User.collection.find( query).toArray().size() == 0 ) {
			User user = new User( name, nickName, pwd);
			WriteResult<User, String> insertion = User.collection.insert( user);
			return insertion.getSavedId();
		}
		else {
			return null;
		}
	}
	
	public static void delete( String id) {
		User user = User.collection.findOneById( id);
		if ( user != null) {
			User.collection.remove( user);
		}
	}
	
	public static boolean auth( String nickName, String pwd) {
		//System.out.println( "NickName = " + nickName + ", Password = " + pwd);
		BasicDBObject query = new BasicDBObject( "nickName", nickName).append( "pwd", pwd);
		//System.out.println( "Size = " + User.collection.find( query).toArray().size() );
		//System.out.println( User.collection.find().toArray() );
		return ( User.collection.find( query).toArray().size() ) == 1;
	}
	
	public static int edit( String id, String name, String nickName, String pwd, String pwd2) {
		User user = User.collection.findOneById( id);
		if ( user != null) {
			if ( pwd.equals( pwd2) ) {
				User updated = new User( name, nickName, pwd);
				User.collection.updateById( id, updated);
				return 0;
			}
			else {
				return 1;
			}
		}
		else {
			return 2;
		}
	}
	
	public static void removeAll() {
		User.collection.drop();
	}
	
	public String toString() {
		return "{\"id\":\"" + id + "\", \"nickName\":\"" + nickName + "\", \"pwd\":\"" + pwd + "\", \"name\":\"" + name + "\"}";
	}
}
