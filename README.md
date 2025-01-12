# How to Build and Run the Java Project

This guide explains how to compile and run the Java project, assuming all `.java` files are located in a single directory.

---

## Prerequisites

1. **Java Development Kit (JDK)**: Ensure you have the JDK installed on your system.
   - You can check if it's installed by running:
     ```bash
     java -version
     ```
   - If not installed, download it from the [official Oracle JDK website](https://www.oracle.com/java/technologies/javase-downloads.html) or use your package manager.

2. **Command Line Access**: Ensure you can access a terminal or command prompt.

---

## Steps to Build and Run the Project

### 1. Navigate to the Project Directory
Open a terminal or command prompt and navigate to the directory containing all your `.java` files. Use the `cd` command:
```bash
cd /path/to/your/java/files
javac *.java
java Main
