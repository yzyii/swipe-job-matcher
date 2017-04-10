Job Matcher
============================

To run, either use the provided gradle wrapper and run task on the command line with: gradlew run
Or run from the JobMatcherApplication.java with the arguments: server server.yml

By default, the server will run on port 8080 while metrics and admin functions will be available on port 8081.
Specific config for metrics and logging can be set in server.yml and extended in the JobMatcherApplication class.

To build a deployable jar, run: gradlew shadowJar
