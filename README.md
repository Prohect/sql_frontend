

# Project Import and Build Guide

This guide will walk you through importing a project from a Version Control System (VCS) into IntelliJ IDEA and using Maven to build the project. Follow these steps to ensure your development environment is correctly set up and ready for development.

## Importing the Project

### From VCS

1. Open IntelliJ IDEA.
2. Select `File` > `New` > `Project from Version Control...`.
3. In the pop-up window, choose the appropriate version control system (e.g., Git), enter the repository URL, and click `Clone`.

### Waiting for Maven to Download Dependencies

1. After the project is imported, IntelliJ IDEA will automatically start downloading all project dependencies using Maven.
2. You can see a progress bar at the right side of the bottom navigation bar, indicating the download process.

## Building Modules

### Building the `sql_frontend_common` Module

1. Once all dependencies are downloaded, go to the Maven UI.
2. In the Maven tool window, find the `sql_frontend_common` module.
3. click on the module and click in `Lifecycle` and sequentially operate:
   - `Clean`
   - `Package`
   - `Install`
4. These steps will install the `sql_frontend_common` module to your local Maven repository.

### Refreshing the Maven Project

1. After completing the above build steps, refresh the Maven project to ensure changes take effect.
2. You can refresh the project by clicking the `Reload All Maven Projects` button at the top of the Maven tool window.

### Verifying Dependencies

1. After refreshing, check that the `sql_frontend` and `sql_frontend_server` modules can find the dependency called `sql_frontend_common`.
2. If everything is correct, these modules should compile successfully.

## Running the Application

### Launching the App

1. Go to `mainui.java` or `server.java`.
2. Use the run button provided by IntelliJ IDEA to launch the application.
3. Modify the source code as needed and re-run the application to see the effects.

## Notes

- **Packaging Output Classes into a .jar**
  - If you want to package the output classes into a .jar file, you must install the newest `sql_frontend_common` module to your local Maven repository first if it has been modified.
  - Otherwise, Maven might throw exceptions and not work properly.
