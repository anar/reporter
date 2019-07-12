# Spam Reporting
Simple, standalone web application challenge for spam protection team. It uses embedded in-memory NoSQL database, embedded Jetty server, Jersey and Vuejs. 

## Building & Running

Run these commands from /client folder to build the front-end:

`yarn install`

`yarn build`

Run this command from root folder (containing pom.xml file) to build the back-end (build script will copy front-end distribution to target directory and integrate it to jar file):

`mvn clean install`

Execute the application:

`java -jar ./target/reporter.jar`

Application is available at: `http://localhost:8080`