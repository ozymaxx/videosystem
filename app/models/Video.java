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

public class Video {
	@Id
	@ObjectId
	public String id;
	public String header;
	public User publisher;
	
	private static JacksonDBCollection<Video, String> collection = MongoDB.getCollection( "videos", Video.class, String.class);
	
	public Video() {
	}
	
	public Video( String header, User publisher) {
		this.header = header;
		this.publisher = publisher;
	}
	
	public static List<Video> all() {
		return Video.collection.find().toArray();
	}
	
	public static List<Video> all( User publisher) {
		BasicDBObject query = new BasicDBObject( "publisher", publisher);
		return Video.collection.find( query).toArray();
	}
	
	public static List<Video> findBetween( int start, int end, User publisher) {
		List<Video> results;
		List<Video> allVideos;
		
		if ( publisher == null) {
			allVideos = all();
		}
		else {
			allVideos = all( publisher);
		}
		
		results = new ArrayList<Video>();
		int x = 0;
		for ( Video video : allVideos) {
			if ( x >= start && x <= end) {
				results.add( video);
			}
			x++;
		}
		return results;
	}
	
	public static String create( Video video) {
		if ( video.header.length() == 0) {
			return null;
		}
		else {
			WriteResult<Video, String> result = Video.collection.insert( video);
			return result.getSavedId();
		}
	}
	
	public static String create( String header, User publisher) {
		if ( header.length() == 0) {
			return null;
		}
		else {
			Video video = new Video( header, publisher);
			WriteResult<Video, String> result = Video.collection.insert( video);
			return result.getSavedId();
		}
	}
	
	public static void delete( String id) {
		Video video = Video.collection.findOneById( id);
		if ( video != null) {
			Video.collection.remove( video);
		}
	}
	
	public static boolean edit( String id, String name, User publisher) {
		Video video = Video.collection.findOneById( id);
		if ( video != null) {
			Video updated = new Video( name, publisher);
			Video.collection.updateById( id, updated);
			return true;
		}
		else {
			return false;
		}
	}
	
	public static void removeAll() {
		Video.collection.drop();
	}
}
