# About
This project is a YouTube clone developed by a team of two, Shon and Amit. It consists of a [website](https://github.com/amitbrown21/Hemi-Tube-DB) and an [Android app](https://github.com/amitbrown21/Hemi-Tube-Android-DB) that aim to replicate the core functionalities of YouTube.

## Building process

Our team started the development process by focusing on the website. We divided the tasks and agreed upon the specific parts of the website each team member would work on. Once each part was completed, we integrated all the components to create a cohesive and functional website. After completing the website, we shifted our attention to the Android app, following a similar approach to create a functional Android app that complements the website.

## Features

- User registration and authentication
- Video upload and management
- Video playback and streaming
- Search functionality
- User comments and likes on videos

## Technologies Used
### Website

- Front-end:

  - HTML5
  - CSS3
  - JavaScript
  - React

### Android App
- Language: Java
- Server: Javascript
- Android SDK
- Room for Database
- MongoDB

## Setup and Installation
### Website

Clone the repository:
   ```bash
   git clone https://github.com/amitbrown21/Hemi-Tube-DB
 ```
Install dependencies:
```bash
npm install mongoose jsonwebtoken
```
Start the development server:
  ```bash
   npm start
  ```
  Then click 'y'
 Open the website in your browser at
   ```bash
    http://localhost:3001
   ```
### Android App
 Clone the repository:
 ```bash
 git clone https://github.com/amitbrown21/Hemi-Tube-Android-DB
  ```
 Open the terminal in '/Server' folder and enter the following
  ```bash
  npm i mongoose jsonwebtoken multer path
  ```
  Then go to '/Scripts' folder and enter:
  ```bash
  node ./uploadUsers.js
  ```
  Then 
  ```bash
  node ./uploadVideos.js
  ```
Go back to the '/Server' folder and start the server with:
```bash
node ./server.js
```
Open the project in Android Studio
 
Sync the project with Gradle files
 
Build and run the app on an Android device or emulator

---

### Contributors

[Shon Trubin](https://github.com/ShonTrubin)  
[Amit Brounstine](https://github.com/amitbrown21)  
