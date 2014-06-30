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
import java.io.File;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }
    
    public static Result login() {
		if ( session().containsKey( "nickName") ) {
			return redirect( routes.Application.mainMenu( null) );
		}
		else {
			return ok( login.render( form( UserLogin.class) ) );
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
			return redirect( routes.Application.mainMenu( null) );
		}
		else if ( loginForm.hasErrors() ) {
			return badRequest( login.render( loginForm) );
		}
		else {
			session().clear();
			session().put( "nickName", loginForm.field( "nick").value() );
			return redirect( routes.Application.mainMenu( null) );
		}
	}
	
	public static Result watchVideo( String id) {
		return redirect( routes.Application.mainMenu( null) );
	}
	
	public static Result search() {
		return redirect( routes.Application.mainMenu( null) );
	}
	
	/*
	public static Result mainMenu() {
		if ( session().containsKey( "nickName") ) {
			User fetched = User.fetchUser( session().get( "nickName") );
			return ok( mainMenu.render( fetched, Video.all( fetched), form( VideoUpload.class), new HashMap<String, String>() ) );
		}
		else {
			return redirect( routes.Application.login() );
		}
	}
	*/
	
	public static Result mainMenu( String error) {
		if ( session().containsKey( "nickName") ) {
			User fetched = User.fetchUser( session().get( "nickName") );
			HashMap<String, String> messages = new HashMap<String, String>();
			String[] errors = { "Video is successfully uploaded!", "Error saving content, please contact the admin.", "The file is not available!"};
			if ( error != null) {
				if ( error.substring( 0,5).equals( "error") ) {
					if ( error.length() == 6) {
						messages.put( "uploadError", errors[ Integer.parseInt( error.substring(5) ) - 1 ]);
					}
				}
			}
			return ok( mainMenu.render( fetched, Video.all( fetched), form( VideoUpload.class), messages ) );
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
				String videoSaved = Video.create( uploadForm.field( "header").value(), fetched );
				
				if ( videoSaved != null) {
					String fileName = videoFile.getFilename();
					String extension = "";
					
					int x = fileName.length() - 1;
					while ( fileName.charAt(x) != '.' ) {
						extension = fileName.charAt(x) + extension;
						x--;
					}
					
					File finalUpload = videoFile.getFile();
					String uploadPath = Play.application().configuration().getString( "upload.path", "/tmp/");
					finalUpload.renameTo( new File( uploadPath + videoSaved + "." + extension ) );
					return redirect( routes.Application.mainMenu( "error1") );
				}
				else {
					return redirect( routes.Application.mainMenu( "error2") );
				}
			}
			else {
				return redirect( routes.Application.mainMenu( "error3") );
			}
		}
		else {
			return redirect( routes.Application.login() );
		}
	}
	
	public static Result registerMenu() {
		if ( session().containsKey( "nickName") ) {
			return redirect( routes.Application.mainMenu( null) );
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

}
