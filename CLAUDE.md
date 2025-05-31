# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A JavaFX desktop application for managing doTERRA scripts for chat and email communications. The app provides a professional interface for customer service representatives to quickly access and copy pre-written responses.

### Core Features
- **Dual-panel system**: Chat Scripts (plain text) and Email Scripts (rich HTML)
- **Hierarchical organization**: Tabs contain buttons, buttons store scripts
- **Drag-and-drop interface**: Reorder buttons within/between tabs, reorder tabs
- **Variable templates**: Use (variable) syntax for dynamic content replacement
- **Customization**: Button colors, tab names, script content
- **Persistence**: Automatic state saving to `doterra_buttons.dat`
- **Context menus**: Right-click management for buttons and tabs
- **Visual feedback**: Highlighted drop zones during drag operations

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

# Skip tests during build
mvn clean package -DskipTests

# Run tests in debug mode (useful for troubleshooting)
mvn test -Dmaven.surefire.debug
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
- **State Persistence**: Chat scripts saved to `doterra_chat_buttons.dat`, Email scripts saved to `doterra_email_buttons.dat`

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
- Uses Serializable models for persistence (separate files for chat and email scripts)
- UUID-based identification for tabs and buttons
- LinkedHashMap preserves tab order, List maintains button order within tabs
- State loading disabled in tests (see ButtonController constructor)

### Testing Infrastructure
- **Unit Tests**: Model and controller logic testing
- **UI Tests**: TestFX-based integration tests for user interactions
- **Drag-Drop Tests**: Specialized tests for reordering functionality
- **Headless Mode**: Tests run without display using glass robot and software rendering

## Variable Templates

The application supports dynamic variable replacement in script content using a simple syntax:

### Syntax
- **Variables**: Use `(variable_name)` to define variables that will prompt for user input
- **Escaped Parentheses**: Use `\(text)` to include literal parentheses that won't be treated as variables

### Examples
```
Hello (customer_name), your order (order_number) is ready for pickup!
```
When clicked, this will show a single dialog with input fields for:
- customer_name
- order_number

```
Use \(parentheses) for formatting but (real_variable) for replacement.
```
This will show a dialog with one input field for "real_variable" and leave the escaped parentheses as literal text.

### Template Editing Workflow
```
Dear (customer_name), your (product) order will be shipped to (address).
```
If you fill in only "customer_name" = "John" and leave the others empty:
```
Dear John, your (product) order will be shipped to (address).
```
This makes it easy to partially fill templates and see remaining variables that need attention.

### Implementation
- Variables are processed when buttons are clicked
- All variables are presented in a single dialog with labeled input fields
- User can cancel variable input to abort the operation
- **Empty Values**: Variables left empty retain their literal text (e.g., `(name)` stays as `(name)`)
- This allows template editing by leaving variables unfilled to see their placeholders
- Both chat scripts (plain text) and email scripts (HTML) support variables
- Variable replacement happens before content is copied to clipboard

## Key Development Notes

### Configuration
- Main class is defined in pom.xml as `com.doterra.app.DoTerraApp`
- Minimum window size: 800x600, default: 1200x800
- Java 17 target with Maven 3.6+ required
- Maven Shade plugin creates executable JAR with dependencies
- State persistence can be disabled for testing by commenting out `loadState()` calls

### Development Tips
- **Module System**: Project uses Java modules - see `module-info.java` for exports/requires
- **TestFX Headless**: Tests run without display using system properties in pom.xml
- **CSS Hot Reload**: Styles in `/styles/main.css` applied at runtime
- **Serialization**: Models must maintain `serialVersionUID` for backward compatibility
- **UUID Usage**: Tabs and buttons use UUIDs for unique identification

### Common Troubleshooting
- **Test Failures**: Ensure no save files exist in test directory
- **UI Tests Hanging**: Check TestFX headless properties in pom.xml
- **Build Issues**: Use `mvn clean` before building if encountering strange errors
- **Runtime Errors**: Verify all JavaFX modules are included in module-info.java