Akash Jewellers Mobile App

Overview The Akash Jewellers mobile application provides real-time updates on precious metal prices to customers. This app allows jewelry enthusiasts and investors to stay informed about current gold and silver rates through a direct connection to the jewellery shop owner's updated pricing information. Features

Real-time Price Updates: Get the latest gold and silver prices directly from the jewellery shop owner Admin Dashboard: Dedicated interface for shop owners to update precious metal prices through Firebase database User Authentication: Secure sign-in/sign-up system with OTP verification Personalized Experience: User accounts to save preferences and track price history Notification System: Alerts for significant price changes or predetermined thresholds Easy Navigation: Simple and intuitive user interface for seamless experience

Technical Details

Development Platform: Android Studio Programming Language: Java Database: Firebase Realtime Database for instant price updates Authentication: Firebase Authentication with phone number and OTP verification UI Design: XML layouts with Material Design components Minimum SDK: Android 5.0 (Lollipop) Target SDK: Android 11+

System Architecture

Admin Side: Shop owners can log in to a protected dashboard to update gold and silver prices Database: Firebase Realtime Database stores the latest price information and pushes updates instantly Client Side: Customer app receives and displays the updated pricing information in real-time

Screenshots The application includes:
Sign-in/Sign-up screens with OTP verification

Customer dashboard displaying current precious metal prices

Admin interface for updating prices (not shown in provided screenshots)

Environment Setup Ensure you have:

Android Studio (latest version recommended) JDK 8 or higher Android SDK with API level 21 or higher Firebase project setup
