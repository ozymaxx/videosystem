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
	
	public static boolean delete( String id) {
		User user = User.collection.findOneById( id);
		if ( user != null && !( user.nickName.equals( "admin") ) ) {
			User.collection.remove( user);
			return true;
		}
		else {
			return false;
		}
	}
	
	public static boolean auth( String nickName, String pwd) {
		//System.out.println( "NickName = " + nickName + ", Password = " + pwd);
		BasicDBObject query = new BasicDBObject( "nickName", nickName).append( "pwd", pwd);
		//System.out.println( "Size = " + User.collection.find( query).toArray().size() );
		//System.out.println( User.collection.find().toArray() );
		return ( User.collection.find( query).toArray().size() ) == 1;
	}
	
	public static User fetchUser( String nickName) {
		BasicDBObject query = new BasicDBObject( "nickName", nickName);
		
		List<User> result = User.collection.find( query).toArray();
		if ( result.size() == 1 ) {
			return result.get(0);
		}
		else {
			return null;
		}
	}
	
	public static boolean edit( String name, String nickName, String pwd) {
		User user = User.collection.find( new BasicDBObject( "nickName", nickName) ).toArray().get(0);
		if ( user != null) {
			User updated = new User( name, nickName, pwd);
			User.collection.updateById( user.id, updated);
			return true;
		}
		else {
			return false;
		}
	}
	
	public static User getUserInfo( String id) {
		return User.collection.findOneById( id);
	}
	
	public static void removeAll() {
		User.collection.drop();
	}
	
	public String toString() {
		return "{\"id\":\"" + id + "\", \"nickName\":\"" + nickName + "\", \"pwd\":\"" + pwd + "\", \"name\":\"" + name + "\"}";
	}
	
	public boolean equals( User u) {
		return u.nickName.equals( nickName) && u.id.equals( id);
	}
}
