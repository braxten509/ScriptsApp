# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A JavaFX desktop application for managing doTERRA scripts for chat and email communications. The app features:
- Sidebar navigation with Chat Scripts and Email Scripts panels
- Chat Scripts: Plain text support for quick responses
- Email Scripts: Rich text editor with HTML formatting
- Customizable buttons to save and access scripts
- Organizable tabs for categorizing buttons
- Right-click context menus for button management

## Commands

### Build and Run
```bash
# Clean and build the project
mvn clean package

# Run the application directly with Maven
mvn javafx:run

# Run the built JAR file
java -jar target/doTERRAApp20-2.0.0.jar
```

### Testing
```bash
# Run tests (JUnit 5)
mvn test
```

## Architecture

### Main Entry Point
- `com.doterra.app.DoTerraApp` - Main application class that extends JavaFX Application
- Creates MainView and sets up the primary stage with CSS styling

### Core Structure
- **MVC Pattern**: Controllers handle navigation, Views manage UI components, Models represent data
- **Main Package**: `com.doterra.app` contains the primary application logic
- **Legacy Package**: `org.bchenay.doterraapp20` contains older HelloApplication (appears unused)

### Key Components
- `MainView` - Root BorderPane with sidebar navigation and content area
- `NavigationController` - Manages switching between Chat and Email panels
- `ChatScriptsPanel` & `EmailScriptsPanel` - Content panels for different script types
- `ButtonTab` - Model for organizing script buttons into tabs
- `ScriptButton` - Model for individual script buttons with content and styling

### UI Framework
- **JavaFX 17.0.6** with additional libraries:
  - ControlsFX for enhanced controls
  - FormsFX for form handling
  - ValidatorFX for input validation
  - TilesFX for dashboard components
- **CSS Styling**: Main stylesheet at `/styles/main.css`
- **Responsive Layout**: BorderPane with resizable sidebar and content areas

### Data Models
- Uses Serializable models for persistence
- UUID-based identification for tabs and buttons
- List-based storage for buttons within tabs

## Key Development Notes

- Main class is defined in pom.xml as `com.doterra.app.DoTerraApp`
- Minimum window size: 800x600, default: 1200x800
- Java 17 target with Maven 3.6+ required
- Maven Shade plugin creates executable JAR with dependencies