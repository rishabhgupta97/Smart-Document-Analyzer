# 🛠️ Setup Guide for Smart Document Analyzer

This guide will help you set up the Smart Document Analyzer project on your local machine. Follow these steps carefully to get the application running.

## 📋 Prerequisites

Before you begin, make sure you have the following installed on your machine:

### Required Software
- **Java 17 or higher** - [Download Oracle JDK](https://www.oracle.com/java/technologies/javase-downloads.html) or [OpenJDK](https://adoptium.net/)
- **Maven 3.6+** - [Download Maven](https://maven.apache.org/download.cgi)
- **Node.js 16+** - [Download Node.js](https://nodejs.org/)
- **npm** (comes with Node.js)
- **Git** - [Download Git](https://git-scm.com/downloads)

### Verify Installations

Run these commands to verify your installations:

```bash
# Check Java version
java -version
# Should show version 17 or higher

# Check Maven version
mvn -version
# Should show version 3.6 or higher

# Check Node.js version
node -v
# Should show version 16 or higher

# Check npm version
npm -v
# Should show version 7 or higher

# Check Git version
git --version
# Should show Git version
```

## 🚀 Quick Start

### Step 1: Clone the Repository

```bash
# Clone the repository
git clone https://github.com/rishabhgupta97/Smart-Document-Analyzer.git

# Navigate to project directory
cd Smart-Document-Analyzer
```

### Step 2: Backend Setup (Spring Boot)

```bash
# Navigate to backend directory
cd backend

# Build the project (this will download dependencies)
mvn clean install

# Run the application
java -jar target/smart-document-analyzer-0.0.1-SNAPSHOT.jar
```

**Alternative way to run backend:**
```bash
# If the above JAR command doesn't work, try:
mvn spring-boot:run
```

✅ **Backend should now be running on: http://localhost:8080**

### Step 3: Frontend Setup (React)

**Open a new terminal window/tab** and run:

```bash
# Navigate to frontend directory (from project root)
cd frontend

# Install dependencies
npm install

# Start the development server
npm start
```

✅ **Frontend should now be running on: http://localhost:3000**

### Step 4: Verify Everything Works

1. **Check Backend Health:**
   ```bash
   curl http://localhost:8080/api/documents/health
   ```
   Should return: `{"status":"UP","service":"Smart Document Analyzer API"}`

2. **Open Frontend:**
   - Go to http://localhost:3000 in your browser
   - You should see the Smart Document Analyzer interface

3. **Test Document Upload:**
   - Use the provided `test-document.txt` in the project root
   - Drag and drop it into the upload area
   - Verify you get analysis results

## 🔧 Development Workflow

### Running Both Servers

You'll need **two terminal windows** open:

**Terminal 1 - Backend:**
```bash
cd backend
java -jar target/smart-document-analyzer-0.0.1-SNAPSHOT.jar
```

**Terminal 2 - Frontend:**
```bash
cd frontend
npm start
```

### Making Changes

**For Backend Changes:**
1. Stop the backend server (Ctrl+C)
2. Make your changes in Java files
3. Rebuild: `mvn clean install`
4. Restart: `java -jar target/smart-document-analyzer-0.0.1-SNAPSHOT.jar`

**For Frontend Changes:**
- The React development server automatically reloads when you save files
- No need to restart

## 🐛 Troubleshooting

### Common Issues and Solutions

#### 1. Port Already in Use
```bash
# Error: Port 8080 is already in use
# Solution: Find and kill the process using the port
lsof -ti:8080 | xargs kill -9

# Error: Port 3000 is already in use  
# Solution: Find and kill the process using the port
lsof -ti:3000 | xargs kill -9
```

#### 2. Java Version Issues
```bash
# Check if you have multiple Java versions
java -version
javac -version

# Set JAVA_HOME (macOS/Linux)
export JAVA_HOME=$(/usr/libexec/java_home -v 17)

# Verify
echo $JAVA_HOME
```

#### 3. Maven Build Failures
```bash
# Clear Maven cache and rebuild
mvn clean
rm -rf ~/.m2/repository
mvn install
```

#### 4. npm Install Issues
```bash
# Clear npm cache
npm cache clean --force

# Delete node_modules and reinstall
rm -rf node_modules
rm package-lock.json
npm install
```

#### 5. CORS Errors
- Make sure backend is running on port 8080
- Make sure frontend is running on port 3000
- Check that both servers are running

#### 6. File Upload Not Working
- Verify backend is running and accessible
- Check browser console for error messages
- Ensure file is PDF, DOCX, or TXT format
- Check file size is under 50MB

### Checking Server Status

**Backend Status:**
```bash
# Check if backend is running
curl http://localhost:8080/api/documents/health

# Check backend logs in the terminal where you started it
```

**Frontend Status:**
- Check the terminal where you ran `npm start`
- Look for compilation errors
- Check browser developer console (F12)

## 📁 Project Structure

```
Smart-Document-Analyzer/
├── backend/                          # Spring Boot Application
│   ├── src/main/java/               # Java source code
│   │   └── com/analyzer/
│   │       ├── controller/          # REST Controllers
│   │       ├── service/             # Business Logic  
│   │       └── model/               # Data Models
│   ├── src/main/resources/          # Configuration files
│   │   └── application.properties   # Spring Boot config
│   ├── target/                      # Build output
│   └── pom.xml                      # Maven dependencies
├── frontend/                        # React Application
│   ├── public/                      # Static files
│   │   └── index.html              # Main HTML file
│   ├── src/                        # React source code
│   │   ├── components/             # React components
│   │   ├── services/               # API service calls
│   │   ├── App.js                  # Main App component
│   │   └── index.js                # Entry point
│   ├── package.json                # npm dependencies
│   └── node_modules/               # npm packages (auto-generated)
├── test-document.txt               # Sample test file
├── README.md                       # Project documentation
├── SETUP.md                        # This setup guide
└── .gitignore                      # Git ignore rules
```

## 🔑 Key Endpoints

### Backend API Endpoints:
- **Health Check**: `GET http://localhost:8080/api/documents/health`
- **Upload Document**: `POST http://localhost:8080/api/documents/upload`
- **Get Analysis**: `GET http://localhost:8080/api/documents/{id}/analysis`
- **Get All Analyses**: `GET http://localhost:8080/api/documents/all`

### Frontend URL:
- **Main Application**: `http://localhost:3000`

## 💡 Development Tips

1. **Use two terminal windows** - one for backend, one for frontend
2. **Keep both servers running** while developing
3. **Check browser console** for frontend errors
4. **Check terminal output** for backend errors  
5. **Use the test document** to verify functionality
6. **Install browser extensions** like React Developer Tools for better debugging

## 📧 Need Help?

If you encounter issues:

1. **Check this troubleshooting section** first
2. **Verify all prerequisites** are properly installed
3. **Check the main README.md** for additional information
4. **Look at terminal output** for specific error messages
5. **Check browser console** for frontend errors

---

**Happy Coding! 🚀**

Once everything is set up, you'll have a fully functional document analysis application running locally!