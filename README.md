# doTERRA App 2.0

An application for managing scripts for doTERRA chat and email communications.

## Features

- Sidebar navigation with different panels
- Chat Scripts panel with plain text support
- Email Scripts panel with rich text support (bold, italic, links, etc.)
- Customizable buttons to save and quickly access scripts
- Organize buttons into tabs by category
- Customize button colors
- Right-click context menu for button management (rename, duplicate, change color, delete)

## Requirements

- Java 17 or higher
- Maven 3.6 or higher

## Building and Running

### Using Maven

1. Build the project:
   ```
   mvn clean package
   ```

2. Run the application:
   ```
   mvn javafx:run
   ```

### Using the JAR file

1. Build the project:
   ```
   mvn clean package
   ```

2. Run the generated JAR file:
   ```
   java -jar target/doTERRAApp20-2.0.0.jar
   ```

## Usage

### Chat Scripts Panel

1. Type or paste text in the bottom text area
2. Click "Create Button" to save the text as a button
3. Give the button a name and select a color
4. Click on the created button to load its content into the text area
5. Right-click on a button to:
   - Rename the button
   - Change its color
   - Duplicate it
   - Delete it

### Email Scripts Panel

1. Create formatted text using the rich text editor at the bottom
2. Click "Create Button" to save the formatted text as a button
3. Give the button a name and select a color
4. Click on the created button to load its content into the rich text editor
5. Right-click on a button to manage it (same options as Chat Scripts)

### Managing Tabs

- Click the "+" button next to the tabs to create a new tab
- Each tab can contain its own set of buttons
- Use tabs to organize buttons by category or purpose

## License

This project is open source and freely available for use and modification.