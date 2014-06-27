package models;

import java.util.*;
import play.modules.mongodb.jackson.MongoDB;
import net.vz.mongodb.jackson.JacksonDBCollection;
import net.vz.mongodb.jackson.Id;
import net.vz.mongodb.jackson.ObjectId;
import org.codehaus.jackson.annotate.JsonProperty;

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
	
	public static List<Video> findBetween( int start, int end) {
		List<Video> allVideos = all();
		List<Video> results = new ArrayList<Video>();
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
		Video.collection.save( video);
		return video.id;
	}
	
	public static String create( String header, User publisher) {
		Video video = new Video( header, publisher);
		Video.collection.save( video);
		return video.id;
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
