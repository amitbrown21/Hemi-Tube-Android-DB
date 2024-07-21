Hemi-Tube
Hemi-Tube is a YouTube clone developed by a team of two, Shon Trubin and Amit Brounstine. This project includes a website and an Android app that replicate the core functionalities of YouTube.

Building Process
We started by focusing on the website, dividing tasks and working on specific components individually. After completing each part, we integrated them to create a cohesive and functional website. Following the website completion, we applied a similar approach to develop a complementary Android app.

Features
User Registration and Authentication
Video Upload and Management
Video Playback and Streaming
Search Functionality
User Comments and Likes on Videos
Technologies Used
Website
Front-end:
HTML5
CSS3
JavaScript
React
Android App
Language: Java
Server: JavaScript
Android SDK
Room for Database
MongoDB
Setup and Installation
Website
Clone the repository:
sh
Copy code
git clone https://github.com/amitbrown21/Hemi-Tube-DB
Install dependencies:
sh
Copy code
npm install
Start the development server:
sh
Copy code
npm start
Open the website in your browser at http://localhost:3000
Android App
Clone the repository:
sh
Copy code
git clone https://github.com/amitbrown21/Hemi-Tube-Android-DB
Open the terminal in the "Server" folder and install dependencies:
sh
Copy code
npm i mongoose jsonwebtoken multer path
Move to the scripts folder and run the setup scripts:
sh
Copy code
cd scripts
node ./uploadUsers
node ./uploadVideos
Open the project in Android Studio.
Sync the project with Gradle files.
Build and run the app on an Android device or emulator.
Contributors
Shon Trubin
Amit Brounstine
