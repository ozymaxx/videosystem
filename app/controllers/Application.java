package controllers;

import play.*;
import play.mvc.*;
import play.mvc.Http.MultipartFormData.FilePart;

import play.data.DynamicForm;
import play.data.Form;
import static play.data.Form.form;

import views.html.*;

import models.*;

import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;

public class Application extends Controller {

    public static Result index() {
        return redirect( routes.Application.login() );
    }
    
    public static Result login() {
		if ( session().containsKey( "nickName") ) {
			return redirect( routes.Application.mainMenu() );
		}
		else {
			return ok( login.render( form( UserLogin.class) ) );
		}
	}
	
	public static Result logout() {
		if ( session().containsKey( "nickName") ) {
			session().clear();
			return redirect( routes.Application.login() );
		}
		else {
			return redirect( routes.Application.login() );
		}
	}
	
	public static Result editProfile() {
		if ( session().containsKey( "nickName") ) {
			User user = User.fetchUser( session().get( "nickName") );
			return ok( userEdit.render( user, form( ProfileEdition.class), new HashMap<String, String>() ) );
		}
		else {
			return redirect( routes.Application.login() );
		}
	}
	
	public static Result commitProfileChanges() {
		Form<ProfileEdition> profileForm = form( ProfileEdition.class).bindFromRequest();
		User user = User.fetchUser( session().get( "nickName") );
		
		if ( profileForm.field( "nickName").value().equals( session().get( "nickName") ) ) {
			if ( profileForm.hasErrors() ) {
				return badRequest( userEdit.render( user, profileForm, new HashMap<String, String>() ) );
			}
			else {
				return redirect( routes.Application.mainMenu() );
			}
		}
		else {
			HashMap<String, String> messages = new HashMap<String, String>();
			messages.put( "validationError", "Ops, you tried to change your nick name!");
			return ok( userEdit.render( user, form( ProfileEdition.class), messages ) );
		}
	}
	
	public static Result authenticate() {
		Form<UserLogin> loginForm = form( UserLogin.class).bindFromRequest();
		UserLogin op = new UserLogin();
		op.nick = loginForm.field( "nick").value();
		op.pwd = loginForm.field( "pwd").value();
		if ( op.validate() == null ) {
			session().clear();
			session().put( "nickName", loginForm.field( "nick").value() );
			return redirect( routes.Application.mainMenu() );
		}
		else if ( loginForm.hasErrors() ) {
			return badRequest( login.render( loginForm) );
		}
		else {
			session().clear();
			session().put( "nickName", loginForm.field( "nick").value() );
			return redirect( routes.Application.mainMenu() );
		}
	}
	
	public static Result watchVideo( String id) {
		if ( session().containsKey( "nickName") ) {
			User user = User.fetchUser( session().get( "nickName") );
			Video video = Video.getVideo( id, user);
			return ok( watchVideo.render( user, video, Video.all( user).size() ) );
		}
		else {
			return redirect( routes.Application.login() );
		}
	}
	
	public static Result searchScreen() {
		if ( session().containsKey( "nickName") ) {
			User fetched = User.fetchUser( session().get( "nickName") );
			return ok( searchScreen.render( fetched, new ArrayList<Video>(), false, form( Search.class) ) );
		}
		else {
			return redirect( routes.Application.login() );
		}
	}
	
	public static Result search() {
		if ( session().containsKey( "nickName") ) {
			Form<Search> searchForm = form( Search.class).bindFromRequest();
			
			User fetched = User.fetchUser( session().get( "nickName") );
			if ( searchForm.hasErrors() ) {
				return badRequest( searchScreen.render( fetched, new ArrayList<Video>(), true, searchForm) );
			}
			else {
				return ok( searchScreen.render( fetched, Video.search( searchForm.field( "phrase").value(), fetched ), true, searchForm) );
			}
		}
		else {
			return redirect( routes.Application.login() );
		}
	}
	
	public static Result mainMenu() {
		if ( session().containsKey( "nickName") ) {
			User fetched = User.fetchUser( session().get( "nickName") );
			HashMap<String, String> messages = new HashMap<String, String>();
			String[] errors = { "Video is successfully uploaded!", "Error saving content, please contact the admin.", "The file is not available!", "There is no such a video!", "Video removed successfully!", "Error converting video, please contact the administrator.", "Video successfully converted!"};
			if ( session().containsKey( "curAddRemoveError") ) {
				String error = session().get( "curAddRemoveError");
				if ( error.length() == 6 ) {
					if ( error.substring( 0,5).equals( "error") ) {
						int num = Integer.parseInt( error.substring(5) ) - 1;
						if ( num == 0 || num == 4 || num == 6) {
							messages.put( "uploadSuccess", errors[num]);
							session().remove( "curAddRemoveError");
						}
						else {
							messages.put( "uploadError", errors[num]);
							session().remove( "curAddRemoveError");
						}
					}
				}
			}
			return ok( mainMenu.render( fetched, Video.all( fetched), form( VideoUpload.class), messages, form( Search.class) ) );
		}
		else {
			return redirect( routes.Application.login() );
		}
	}
	
	public static Result addVideo() {
		if ( session().containsKey( "nickName") ) {
			Form<VideoUpload> uploadForm = form( VideoUpload.class).bindFromRequest();
			FilePart videoFile = request().body().asMultipartFormData().getFile( "videoFile");
			
			if ( videoFile != null) {
				User fetched = User.fetchUser( session().get( "nickName") );
				
				String fileName = videoFile.getFilename();
				String extension = "";
				
				int x = fileName.length() - 1;
				while ( fileName.charAt(x) != '.' ) {
					extension = fileName.charAt(x) + extension;
					x--;
				}
				
				String videoSaved = Video.create( uploadForm.field( "header").value(), fetched, extension );
				
				if ( videoSaved != null) {
					File finalUpload = videoFile.getFile();
					String uploadPath = Play.application().configuration().getString( "upload.path");
					finalUpload.renameTo( new File( uploadPath, videoSaved + "." + extension ) );
					session().put( "curAddRemoveError", "error1");
					return redirect( routes.Application.mainMenu() );
				}
				else {
					session().put( "curAddRemoveError", "error2");
					return redirect( routes.Application.mainMenu() );
				}
			}
			else {
				session().put( "curAddRemoveError", "error3");
				return redirect( routes.Application.mainMenu() );
			}
		}
		else {
			return redirect( routes.Application.login() );
		}
	}
	
	public static Result removeVideo( String id) {
		User user = User.fetchUser( session().get( "nickName") );
		String ext = Video.getExtensionOf( id);
		if ( Video.remove( id, user) ) {
			File videoFile = new File( Play.application().configuration().getString( "upload.path"), id + "." + ext);
			if ( videoFile.delete() ) {
				session().put( "curAddRemoveError", "error5");
				return redirect( routes.Application.mainMenu() );
			}
			else {
				session().put( "curAddRemoveError", "error4");
				return redirect( routes.Application.mainMenu() );
			}
		}
		else {
			return redirect( routes.Application.mainMenu() );
		}
	}
	
	public static Result convertVideo( String id, String newFormat) {
		if ( session().containsKey( "nickName") ) {
			User user = User.fetchUser( session().get( "nickName") );
			Video check = Video.getVideo( id, user);
			if ( check != null) {
				// conversion process goes here...
				session().put( "curAddRemoveError", "error7");
				return redirect( routes.Application.mainMenu() );
			}
			else {
				session().put( "curAddRemoveError", "error6");
				return redirect( routes.Application.mainMenu() );
			}
		}
		else {
			return redirect( routes.Application.login() );
		}
	}
	
	public static Result registerMenu() {
		if ( session().containsKey( "nickName") ) {
			return redirect( routes.Application.mainMenu() );
		}
		else {
			return ok( registrationForm.render( form( UserRegistration.class) ) );
		}
	}
	
	public static Result registerUser() {
		Form<UserRegistration> regForm = form( UserRegistration.class).bindFromRequest();
		if ( regForm.hasErrors() ) {
			return badRequest( registrationForm.render( regForm) );
		}
		else {
			return redirect( routes.Application.login() );
		}
	}
	
	public static Result adminUsers() {
		if ( session().containsKey( "nickName") ) {
			if ( session().get( "nickName").equals( "admin") ) {
				User admin = User.fetchUser( session().get( "nickName") );
				HashMap<String, String> messages = new HashMap<String, String>();
				String[] errors = { "User info successfully changed!", "Error removing user, please contact the admin.", "User successfully removed!"};
				if ( session().containsKey( "adminErrors") ) {
					String error = session().get( "adminErrors");
					if ( error.length() == 6 ) {
						if ( error.substring( 0,5).equals( "error") ) {
							int num = Integer.parseInt( error.substring(5) ) - 1;
							if ( num == 0 || num == 2) {
								messages.put( "adminSuccess", errors[num]);
								session().remove( "adminErrors");
							}
							else {
								messages.put( "adminError", errors[num]);
								session().remove( "adminErrors");
							}
						}
					}
				}
				return ok( usersAdmin.render( User.all(), messages, Video.all( User.fetchUser( session().get( "nickName") ) ).size(), admin) );
			}
			else {
				return redirect( routes.Application.mainMenu() );
			}
		}
		else {
			return redirect( routes.Application.login() );
		}
	}
	
	public static Result editUser( String id) {
		if ( session().containsKey( "nickName") ) {
			if ( session().get( "nickName").equals( "admin") ) {
				User toBeEdited = User.getUserInfo( id);
				if ( toBeEdited != null) {
					if ( !( session().containsKey( "userToBeEdited") ) ) {
						session().put( "userToBeEdited", toBeEdited.nickName);
					}
					return ok( userEdition.render( toBeEdited, form( ProfileEdition.class) ) );
				}
				else {
					return redirect( routes.Application.adminUsers() );
				}
			}
			else {
				return redirect( routes.Application.mainMenu() );
			}
		}
		else {
			return redirect( routes.Application.login() );
		}
	}
	
	public static Result changeUser() {
		if ( session().containsKey( "nickName") ) {
			if ( session().get( "nickName").equals( "admin") ) {
				Form<ProfileEdition> changeForm = form( ProfileEdition.class).bindFromRequest();
				String nickEdited = session().get( "userToBeEdited");
				session().remove( "userToBeEdited");
				if ( nickEdited.equals( changeForm.field( "nickName").value() ) ) {
					User toBeEdited = User.fetchUser( changeForm.field( "nickName").value() );
					if ( changeForm.hasErrors() ) {
						return badRequest( userEdition.render( toBeEdited, changeForm) );
					}
					else {
						session().put( "adminErrors", "error1");
						return redirect( routes.Application.adminUsers() );
					}
				}
				else {
					return redirect( routes.Application.adminUsers() );
				}
			}
			else {
				return redirect( routes.Application.mainMenu() );
			}
		}
		else {
			return redirect( routes.Application.login() );
		}
	}
	
	public static Result removeUser( String id) {
		if ( session().containsKey( "nickName") ) {
			if ( session().get( "nickName").equals( "admin") ) {
				User deleted = User.getUserInfo( id);
				if ( !( deleted.nickName.equals( "admin") ) ) {
					if ( User.delete( id) ) {
						session().put( "adminErrors", "error3");
						return redirect( routes.Application.adminUsers() );
					}
					else {
						session().put( "adminErrors", "error2");
						return redirect( routes.Application.adminUsers() );
					}
				}
				else {
					return redirect( routes.Application.adminUsers() );
				}
			}
			else {
				return redirect( routes.Application.mainMenu() );
			}
		}
		else {
			return redirect( routes.Application.login() );
		}
	}
	
	public static Result adminVideos() {
		return redirect( routes.Application.mainMenu() );
	}
	
	public static class UserLogin {
		public String nick;
		public String pwd;
		
		public String validate() {
			if ( User.auth( nick, pwd) ) {
				return null;
			}
			else {
				return "Invalid nick name or password!";
			}
		}
	}
	
	public static class UserRegistration {
		public String name;
		public String pwd1;
		public String pwd2;
		public String nickName;
		
		public String validate() {
			if ( !( pwd1.equals( pwd2) ) ) {
				return "The passwords must match!";
			}
			else {
				if ( User.create( name, nickName, pwd1) != null ) {
					return null;
				}
				else {
					return "The given nick name is already in use, please choose a different one.";
				}
			}
		}
	}
	
	public static class VideoUpload {
		public String header;
	}
	
	public static class ProfileEdition {
		public String nickName;
		public String newName;
		public String newPwd1;
		public String newPwd2;
		
		public String validate() {
			if ( newName.length() >= 2) {
				if ( newPwd1.equals( newPwd2) ) {
					if ( newPwd1.length() < 6) {
						return "The password must have at least 6 characters!";
					}
					else {
						if ( User.edit( newName, nickName, newPwd1) ) {
							return null;
						}
						else {
							return "No user found!";
						}
					}
				}
				else {
					return "The passwords must match!";
				}
			}
			else {
				return "Your name should have at least 2 characters!";
			}
		}
	}
	
	public static class Search {
		public String phrase;
		
		public String validate() {
			if ( phrase.length() > 0) {
				return null;
			}
			else {
				return "Please enter a non-empty phrase!";
			}
		}
	}

}
