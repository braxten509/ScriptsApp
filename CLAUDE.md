# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A JavaFX desktop application for managing doTERRA scripts for chat and email communications. The app features:
- Sidebar navigation with Chat Scripts and Email Scripts panels
- Chat Scripts: Plain text support for quick responses
- Email Scripts: Rich text editor with HTML formatting
- Customizable buttons to save and access scripts
- Organizable tabs for categorizing buttons
- Drag-and-drop reordering for tabs and buttons
- Right-click context menus for button management
- Visual feedback during drag operations

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
# Run all tests (JUnit 5 + TestFX for UI tests)
mvn test

# Run specific test class
mvn test -Dtest=ButtonControllerTest

# Run tests with specific pattern
mvn test -Dtest="*DragDrop*"
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
- `ChatScriptsPanel` & `EmailScriptsPanel` - Content panels with drag-and-drop support
- `ButtonController` - Manages tabs and buttons, handles reordering operations
- `ButtonTab` - Model for organizing script buttons into tabs
- `ScriptButton` - Model for individual script buttons with content and styling

### Drag-and-Drop Architecture
- **Button Reordering**: Buttons can be dragged within tabs and between tabs
- **Tab Reordering**: Tab headers support drag-and-drop for reorganization
- **Visual Feedback**: Drop zones highlighted during drag operations
- **State Persistence**: Button and tab order automatically saved to `doterra_buttons.dat`

### UI Framework
- **JavaFX 17.0.6** with additional libraries:
  - ControlsFX for enhanced controls
  - FormsFX for form handling
  - ValidatorFX for input validation
  - TilesFX for dashboard components
- **TestFX 4.0.18** for UI testing with headless support
- **CSS Styling**: Main stylesheet at `/styles/main.css`
- **Responsive Layout**: BorderPane with resizable sidebar and content areas

### Data Models
- Uses Serializable models for persistence in `doterra_buttons.dat`
- UUID-based identification for tabs and buttons
- LinkedHashMap preserves tab order, List maintains button order within tabs
- State loading disabled in tests (see ButtonController constructor)

### Testing Infrastructure
- **Unit Tests**: Model and controller logic testing
- **UI Tests**: TestFX-based integration tests for user interactions
- **Drag-Drop Tests**: Specialized tests for reordering functionality
- **Headless Mode**: Tests run without display using glass robot and software rendering

## Key Development Notes

- Main class is defined in pom.xml as `com.doterra.app.DoTerraApp`
- Minimum window size: 800x600, default: 1200x800
- Java 17 target with Maven 3.6+ required
- Maven Shade plugin creates executable JAR with dependencies
- State persistence can be disabled for testing by commenting out `loadState()` calls