# VueJS + NoSQL + Jetty + Jersey
My approach to a challenge about creating a very simple (coding less than 3 hours), standalone web application for spam protection team. It showcases embedded NoSQL database, embedded Jetty server, Jersey REST and VueJS.

## Building & Running

Run these commands from /frontend folder to build the front-end:

`yarn install`

`yarn build`

Run this command from root folder (containing pom.xml file) to build the back-end (build script will copy front-end distribution to target directory and integrate it to jar file):

`mvn clean install`

Execute the application:

`java -jar ./target/reporter.jar`

Application is available at: `http://localhost:8080`
