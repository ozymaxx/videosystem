## Videosystem

Videosystem is a basic Play-framework based video-management system.

It corresponds the following use cases:
 * Registration for a user
 * Adding / removing videos
 * Watching own videos
 * Managing all videos uploaded to the system (if you are the admin)
 * Managing all users registered to the system (if you are the admin)
 
## Requirements
 * Play framework with 2.2.0 version,
 * MongoDB database server.
 
## Configuration
 * Please open "conf/application.conf" and configure MongoDB connection information as follows: mongodb:servers="<desired_address>:<desired_port>" and mongodb.database="<database_name>"
 * Also, in the same file, configure the upload.path variable as what you want.
 
## Running the application
 * After you put the application under the directory of Play installation, in Unix, type "../play run" in the console. Afterwards, just type "localhost:9000" to see the result.

## Basic user hierarchy

This system has only one admin, named as "admin". Without generating this super user, the system cannot be used. To generate the admin:
 * Type "<localhostaddress>/admin_installation"
 * Then press "Generate" button. You will be redirected to the login page, if successful.
 
## Contact
This program is not tested widely, it may contain some problematic bugs. In the case of any bug, feel free to contact me: ozymaxx@gmail.com
