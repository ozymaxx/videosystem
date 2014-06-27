package controllers;

import play.*;
import play.mvc.*;

import play.data.DynamicForm;
import play.data.Form;
import static play.data.Form.form;

import views.html.*;

import models.*;

public class Application extends Controller {

    public static Result index() {
        return ok(index.render("Your new application is ready."));
    }
    
    public static Result login() {
		if ( session().containsKey( "nickName") ) {
			return redirect( routes.Application.mainMenu() );
		}
		else {
			return ok( login.render( form( UserLogin.class) ) );
		}
	}
	
	public static Result mainMenu() {
		return ok( mainMenu.render() );
	}
	
	public static Result authenticate() {
		Form<UserLogin> loginForm = form( UserLogin.class).bindFromRequest();
		if ( loginForm.hasErrors() ) {
			return badRequest( login.render( loginForm) );
		}
		else {
			return ok( index.render( "S.A.") );
			/*
			session().clear();
			session( "nickName", loginForm.get().nick);
			return redirect( routes.Application.mainMenu() );
			*/
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
	
	public static class UserLogin {
		public String nick;
		public String pwd;
		
		public String validate() {
			System.out.println( nick + "-" + pwd);
			return null;
			/*
			if ( User.auth( nick, pwd) ) {
				return null;
			}
			else {
				return "Invalid user or password!";
			}
			*/
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

}
